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
import com.abysl.harryplotter.model.drives.CacheDrive
import com.abysl.harryplotter.model.jobs.PlotJob
import com.abysl.harryplotter.model.drives.Drive
import com.abysl.harryplotter.model.drives.TempDrive
import com.abysl.harryplotter.util.IOUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
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
        clearTempDirs()
        updateGlobalStagger()
        cancelManager()
        Prefs.startStaggerManager = true
        jobs.value.filter { !it.isRunning() }.forEach {
            it.tempDone = 0
            it.manageSelf = false
        }
        managerScope.launch {
            while (true) {
                if (checkGlobal()) {
                    // If no drives are added, just start plots normally
                    if (drives.value.isEmpty()) {
                        managedJobs()
                            .firstOrNull { it.isReady() }
                            ?.start(cache = randomCacheDrive()?.drivePath)
                        lastStart.value = Clock.System.now()
                    } else {
                        startJobPerReadyDrive()
                    }
                }
                delay(DELAY)
            }
        }
    }

    fun stop() {
        cancelManager()
    }

    fun randomCacheDrive(): CacheDrive?{
        return drives.value.filterIsInstance<CacheDrive>().randomOrNull()
    }

    fun startJobPerReadyDrive() {
        // Maps the drives to the list of jobs that have temp dirs corresponding to them
        val driveMap = drivesToJobs()
        driveMap.keys
            // Get only the drives that are ready to start a job
            .filter { it is TempDrive && checkDrive(it, driveMap[it]) }
            .forEach { drive ->
                // Start one job for each drive that is ready
                val job = driveMap.getOrDefault(drive, emptyList())
                    .firstOrNull { it.isReady() }
                job?.let {
                    it.start(cache = randomCacheDrive()?.drivePath)
                    // Record what time that drive last started a job to keep track for the static stagger
                    driveStartMap.value += drive to Clock.System.now()
                }
            }
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

    private fun checkDrive(drive: TempDrive, driveJobs: List<PlotJob>?): Boolean {
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

    private fun clearTempDirs() {
        val plotJobs = jobs.value
        val plotIds = plotJobs.filter {
            it.state.plotId.isNotBlank()
        }.map { it.state.plotId }
        val dirs = plotJobs.map { it.description.tempDir }
        dirs.forEach { dir ->
            val files = dir.listFiles() ?: return@forEach
            files.filter { file ->
                file.extension == "tmp" &&
                    plotIds.none {
                        file.name.contains(it)
                    }
            }.forEach(IOUtil::deleteFile)
        }
    }
}
