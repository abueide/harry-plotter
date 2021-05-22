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

package com.abysl.harryplotter.viewmodel

import com.abysl.harryplotter.config.Config
import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.model.PlotJob
import com.abysl.harryplotter.model.records.JobDescription
import com.abysl.harryplotter.util.invoke
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class JobsListViewModel {
    val plotJobs: MutableStateFlow<List<PlotJob>> = MutableStateFlow(listOf())
    val selectedPlotJob: MutableStateFlow<PlotJob?> = MutableStateFlow(null)

    var staggerJob = CoroutineScope(Dispatchers.IO)

    fun onStartAll(delay: Long = 1000) {
        cancelStagger()
        staggerJob.launch {
            var staticTimer = Prefs.staticStagger * MILLIS_PER_MINUTE
            plotJobs.value
                .filter { !it.state.running }
                .forEach {
                    while (checkPhaseBlocked() || staticTimer < Prefs.staticStagger * MILLIS_PER_MINUTE) {
                        println("$staticTimer, ${Prefs.staticStagger * MILLIS_PER_MINUTE}")
                        delay(delay)
                        staticTimer += delay
                    }
                    staticTimer = 0L
                    it.start()
                }
        }
    }

    fun onStopAll() {
        cancelStagger()
        plotJobs.value.filter { it.state.running }.forEach { it.stop() }
    }

    fun saveJob(description: JobDescription) {
        val selectedJob = selectedPlotJob()
        if (selectedJob == null) {
            val newJob = PlotJob(description)
            plotJobs.value += PlotJob(description)
            selectedPlotJob.value = newJob
        } else {
            selectedJob.description = description
        }
        Config.savePlotJobs(plotJobs())
    }

    fun cancelStagger() {
        staggerJob.cancel()
        staggerJob = CoroutineScope(Dispatchers.IO)
    }

    fun checkPhaseBlocked(): Boolean {
        return checkPhaseOneBlocked() || checkOtherBlocked()
    }

    fun checkPhaseOneBlocked(): Boolean {
        val phaseOneStagger = Prefs.firstStagger
        if(phaseOneStagger == 0) return false
        return plotJobs.value.filter { it.state.phase == 1 && it.state.running }.size >= phaseOneStagger
    }

    fun checkOtherBlocked(): Boolean {
        val otherStagger = Prefs.otherStagger
        if(otherStagger == 0) return false
        return plotJobs.value.filter { it.state.phase != 1 && it.state.running }.size >= otherStagger
    }

    companion object {
        private const val MILLIS_PER_MINUTE = 60000L
    }
}
