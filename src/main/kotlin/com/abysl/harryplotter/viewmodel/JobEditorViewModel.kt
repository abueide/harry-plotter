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
import javafx.beans.property.SimpleStringProperty
import java.io.File

class JobEditorViewModel(val mainViewModel: MainViewModel) {
    val jobName = SimpleStringProperty()
    val tempDir = SimpleStringProperty()
    val destDir = SimpleStringProperty()
    val threads = SimpleStringProperty()
    val ram = SimpleStringProperty()
    val plotsToFinish = SimpleStringProperty()

    init {
        loadJob(mainViewModel.selectedPlotJob.get())
        mainViewModel.selectedPlotJob.addListener { observable, old, new ->
            if(new == null){
                clearJob()
            }else {
                loadJob(new)
            }
        }
    }

    fun loadJob(plotJob: PlotJob?) {
        plotJob ?: return
        val desc = plotJob.desc
        jobName.set(desc.name)
        tempDir.set(desc.tempDir.path)
        destDir.set(desc.destDir.path)
        threads.set(desc.threads.toString())
        ram.set(desc.ram.toString())
        plotsToFinish.set(desc.plotsToFinish.toString())
    }

    fun clearJob() {
        jobName.set("")
        tempDir.set("")
        destDir.set("")
        threads.set("")
        ram.set("")
        plotsToFinish.set("")
    }

    fun getJob(): PlotJob? {
        val tempDirPath = tempDir.get()
        val destDirPath = destDir.get()
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
        val selectedKey = mainViewModel.selectedKey.get()
        if (selectedKey == null) {
            SimpleDialogs.showAlert("Key Not Selected", "Please add and select a key")
            return null
        }
        val name = jobName.get().ifBlank { "Plot Job ${mainViewModel.plotJobs.count() + 1}" }
        val selectedJob = mainViewModel.selectedPlotJob.get()
        val stats = if (selectedJob == null) JobStats() else selectedJob.stats
        val newDescription = JobDescription(
            name, File(tempDirPath), File(destDirPath),
            threads.get().ifBlank { "0" }.toInt(),
            ram.get().ifBlank { "0" }.toInt(),
            selectedKey,
            plotsToFinish.get().ifBlank { "0" }.toInt()
        )
        return PlotJob(newDescription, stats)
    }

    fun onEdit() {
        val oldKey = mainViewModel.selectedKey.get() ?: return
        KeyEditorWindow(oldKey).show { newKey ->
            if (newKey != null) {
                mainViewModel.chiaKeys.remove(oldKey)
                mainViewModel.chiaKeys.add(newKey)
            }
        }
    }

    fun onAdd() {
        KeyEditorWindow().show {
            if (it != null) mainViewModel.chiaKeys.add(it)
        }
    }

    fun onCancel() {
        jobName.set("")
        tempDir.set("")
        destDir.set("")
        threads.set("")
        ram.set("")
        mainViewModel.selectedKey.set(null)
    }

    fun onSave() {
        val selectedJob = mainViewModel.selectedPlotJob.get()
        val newJob = getJob()
        if(selectedJob == null){
            newJob?.let { mainViewModel.plotJobs.add(it) }
        }else {
        }
    }
}