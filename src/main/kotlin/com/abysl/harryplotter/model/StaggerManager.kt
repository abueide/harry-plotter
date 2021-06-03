/*
 *     Copyright (c) 2021 Andrew Bueide
 *
 *     This file is part of Harry Plotter.
 *
 *     Harry Plotter is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Harry Plotter is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Harry Plotter.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.abysl.harryplotter.model

import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.model.records.Drive
import com.abysl.harryplotter.model.records.StaggerSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

private const val DELAY = 1000L // milliseconds
class StaggerManager(val jobs: MutableStateFlow<List<PlotJob>>, val drives: MutableStateFlow<List<Drive>>) {
    private var managerScope = CoroutineScope(Dispatchers.IO)
    private val globalStagger = MutableStateFlow(StaggerSettings())
    private val lastStart: MutableStateFlow<Instant?> = MutableStateFlow(null)
    private val driveStartMap: MutableStateFlow<Map<Drive, Instant>> = MutableStateFlow(emptyMap())

    init {
        updateGlobalStagger()
    }

    fun start() {
        updateGlobalStagger()
        cancelManager()
        Prefs.startStaggerManager = true
        managedJobs().forEach { it.tempDone = 0 }
        managerScope.launch {
            while (true) {
                if (checkGlobal()) {
                    // Maps the drives to the list of jobs that have temp dirs corresponding to them
                    val driveMap = drivesToJobs()
                    driveMap.keys
                            // Get only the drives that are ready to start a job
                        .filter { checkDrive(it, driveMap[it]) }
                        .forEach { drive ->
                            // Start one job for each drive that is ready
                            val job = driveMap.getOrDefault(drive, emptyList()).firstOrNull { it.isReady() }
                            job?.let {
                                it.start()
                                // Record what time that drive last started a job to keep track for the static stagger
                                driveStartMap.value += drive to Clock.System.now()
                            }
                        }
                }
                delay(DELAY)
            }
        }
    }

    fun stop() {
        cancelManager()
    }

    fun updateGlobalStagger() {
        globalStagger.value = StaggerSettings(
            Prefs.firstStagger, Prefs.otherStagger, Prefs.maxTotal,
            Prefs.staticStagger, Prefs.ignoreStatic
        )
    }

    private fun cancelManager() {
        managerScope.cancel()
        managerScope = CoroutineScope(Dispatchers.IO)
    }



    private fun managedJobs(): List<PlotJob> {
        return jobs.value.filter { !it.manageSelf }
    }

    private fun runningJobs(): List<PlotJob> {
        return jobs.value.filter { !it.manageSelf && it.isRunning() }
    }

    private fun checkDrive(drive: Drive, driveJobs: List<PlotJob>?): Boolean {
        val runningJobs = driveJobs?.filter(PlotJob::isRunning) ?: emptyList()
        val lastTime = driveStartMap.value[drive]
        return drive.staggerSettings.check(lastTime, runningJobs)
    }

    private fun checkGlobal(): Boolean {
        return globalStagger.value.check(lastStart.value, runningJobs())
    }

    private fun drivesToJobs(): Map<Drive, List<PlotJob>> {
        val jobs = managedJobs()
        val mapValues = drives.value.map { drive ->
            drive to jobs.filter { job ->
                job.description.tempDir.toPath().startsWith(drive.drivePath.toPath())
            }
        }
        return mapOf(*mapValues.toTypedArray())
    }
}