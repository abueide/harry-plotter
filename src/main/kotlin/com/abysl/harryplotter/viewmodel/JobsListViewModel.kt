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
import com.abysl.harryplotter.util.IOUtil
import com.abysl.harryplotter.util.invoke
import kotlinx.coroutines.flow.MutableStateFlow

class JobsListViewModel {
    val plotJobs: MutableStateFlow<List<PlotJob>> = MutableStateFlow(listOf())
    val selectedPlotJob: MutableStateFlow<PlotJob?> = MutableStateFlow(null)

    lateinit var refreshCallback: () -> Unit
    lateinit var startStaggerManager: () -> Unit
    lateinit var stopStaggerManager: () -> Unit


    fun onStartAll() {
        startStaggerManager()
    }

    fun forceStopAll(block: Boolean = false) {
        Prefs.startStaggerManager = false
        stopStaggerManager()
        plotJobs.value.forEach { it.stop(block = block) }
    }

    fun gracefulStopAll() {
        stopStaggerManager()
        plotJobs.value.forEach { it.manageSelf = false }
    }

    fun onClear() {
        val plotIds = plotJobs.value.filter {
            it.state.plotId.isNotBlank()
        }.map { it.state.plotId }
        val dirs = plotJobs.value.map { it.description.tempDir }
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
        refreshCallback()
    }

    fun clearSelected() {
        selectedPlotJob.value = null
    }
}
