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

import com.abysl.harryplotter.model.JobStats
import com.abysl.harryplotter.model.PlotJob
import com.abysl.harryplotter.model.records.JobDescription
import com.abysl.harryplotter.windows.KeyEditorWindow
import com.abysl.harryplotter.windows.SimpleDialogs
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class JobEditorViewModel(val mainViewModel: MainViewModel) {
    val jobName = MutableStateFlow("")
    val tempDir = MutableStateFlow("")
    val destDir = MutableStateFlow("")
    val threads = MutableStateFlow("")
    val ram = MutableStateFlow("")
    val plotsToFinish = MutableStateFlow("")

    init {
        loadJob(mainViewModel.selectedPlotJob.value)
        mainViewModel.selectedPlotJob.onEach {
            if(it == null){
                clearJob()
            }else {
                loadJob(it)
            }
        }.launchIn(CoroutineScope(Dispatchers.IO))
    }

    fun loadJob(plotJob: PlotJob?) {
        plotJob ?: return
            val desc = plotJob.desc
            jobName.value = desc.name
            tempDir.value = desc.tempDir.path
            destDir.value = desc.destDir.path
            threads.value = desc.threads.toString()
            ram.value = desc.ram.toString()
            plotsToFinish.value = desc.plotsToFinish.toString()
    }

    fun clearJob() {
        jobName.value = ""
        tempDir.value = ""
        destDir.value = ""
        threads.value = ""
        ram.value = ""
        plotsToFinish.value = ""
    }

    fun getJob(): PlotJob? {
        val tempDirPath = tempDir.value
        val destDirPath = destDir.value
        if (tempDirPath.isBlank() || destDirPath.isBlank()) {
            SimpleDialogs.showAlert(
                "Directory Not Selected",
                "Please make sure to select a destination & temporary directory."
            )
            return null
        }
        val temp = File(tempDirPath)
        val dest = File(destDirPath)
        if (!temp.exists()) {
            SimpleDialogs.showAlert("Temp Directory Does Not Exist", "Please select a valid directory.")
            return null
        }
        if (!dest.exists()) {
            SimpleDialogs.showAlert("Destination Directory Does Not Exist", "Please select a valid directory.")
            return null
        }
        if (!temp.isDirectory) {
            SimpleDialogs.showAlert("Selected Temp Is Not A Directory", "Please select a valid directory.")
            return null
        }
        if (!dest.isDirectory) {
            SimpleDialogs.showAlert("Selected Destination Is Not A Directory", "Please select a valid directory.")
            return null
        }
        val selectedKey = mainViewModel.selectedKey.value
        if (selectedKey == null) {
            SimpleDialogs.showAlert("Key Not Selected", "Please add and select a key")
            return null
        }
        val name = jobName.value.ifBlank { "Plot Job ${mainViewModel.plotJobs.value.size + 1}" }
        val selectedJob = mainViewModel.selectedPlotJob.value
        val stats = if (selectedJob == null) JobStats() else selectedJob.stats
        val newDescription = JobDescription(
            name, File(tempDirPath), File(destDirPath),
            threads.value.ifBlank { "0" }.toInt(),
            ram.value.ifBlank { "0" }.toInt(),
            selectedKey,
            plotsToFinish.value.ifBlank { "0" }.toInt()
        )
        return PlotJob(newDescription, stats)
    }

    fun onEdit() {
        val oldKey = mainViewModel.selectedKey.value ?: return
        KeyEditorWindow(oldKey).show { newKey ->
            if (newKey != null) {
                mainViewModel.chiaKeys.value -= oldKey
                mainViewModel.chiaKeys.value += newKey
            }
        }
    }

    fun onAdd() {
        KeyEditorWindow().show {
            if (it != null) mainViewModel.chiaKeys.value += it
        }
    }

    fun onCancel() {
        jobName.value = ""
        tempDir.value = ""
        destDir.value = ""
        threads.value = ""
        ram.value = ""
        mainViewModel.selectedKey.value = null
    }

    fun onSave() {
        val selectedJob = mainViewModel.selectedPlotJob.value
        val newJob = getJob()
        if(selectedJob == null){
            newJob?.let { mainViewModel.plotJobs.value += it }
        }else {
        }
    }
}