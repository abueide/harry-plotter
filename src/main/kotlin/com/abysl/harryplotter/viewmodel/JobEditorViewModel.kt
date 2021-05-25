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

import com.abysl.harryplotter.model.PlotJob
import com.abysl.harryplotter.model.records.ChiaKey
import com.abysl.harryplotter.model.records.JobDescription
import com.abysl.harryplotter.util.invoke
import com.abysl.harryplotter.util.unwords
import com.abysl.harryplotter.windows.KeyEditorWindow
import com.abysl.harryplotter.windows.SimpleDialogs
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

class JobEditorViewModel {
    val jobName = MutableStateFlow("")
    val tempDir = MutableStateFlow("")
    val destDir = MutableStateFlow("")
    val threads = MutableStateFlow("")
    val kSize = MutableStateFlow("")
    val additionalParams = MutableStateFlow("")
    val ram = MutableStateFlow("")
    val plotsToFinish = MutableStateFlow("")

    val chiaKeys: MutableStateFlow<List<ChiaKey>> = MutableStateFlow(listOf())
    val selectedKey: MutableStateFlow<ChiaKey?> = MutableStateFlow(null)

    private lateinit var savedCallback: (JobDescription) -> Unit
    private lateinit var cancelCallback: () -> Unit

    fun initialized(savedCallback: (JobDescription) -> Unit, selectCallback: () -> Unit) {
        this.savedCallback = savedCallback
        this.cancelCallback = selectCallback
    }

    fun loadJob(plotJob: PlotJob?) {
        plotJob ?: return
        val desc = plotJob.description
        selectedKey.value = desc.key
        jobName.value = desc.name
        tempDir.value = desc.tempDir.path
        destDir.value = desc.destDir.path
        kSize.value = if(desc.kSize == 32) "" else desc.kSize.toString()
        additionalParams.value = desc.additionalParams.unwords()
        threads.value = if(desc.threads == 0) "" else desc.threads.toString()
        ram.value = if(desc.ram == 0) "" else desc.ram.toString()
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

    fun onEdit() {
        val oldKey = selectedKey() ?: return
        KeyEditorWindow(oldKey).show { newKey ->
            if (newKey != null) {
                chiaKeys.value -= oldKey
                chiaKeys.value += newKey
            }
        }
    }

    fun onAdd() {
        KeyEditorWindow().show {
            if (it != null) chiaKeys.value += it
        }
    }

    fun onNew() {
        onCancel()
        onSave(false)
    }

    fun onCancel() {
        jobName.value = ""
        tempDir.value = ""
        destDir.value = ""
        threads.value = ""
        ram.value = ""
        selectedKey.value = chiaKeys.value.firstOrNull()
        cancelCallback()
    }

    fun onSave(check: Boolean = true) {
        val newDesc = getJobDescription(check = check) ?: return
        savedCallback(newDesc)
    }

    fun getJobDescription(defaultName: String = "Unnamed Job", check: Boolean = true): JobDescription? {
        val tempDirPath = tempDir.value
        val destDirPath = destDir.value
        if (check) {
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
        }
        val key = selectedKey()
        if (key == null) {
            SimpleDialogs.showAlert("Key Not Selected", "Please add and select a key")
            return null
        }
        val name = jobName.value.ifBlank { defaultName }
        val newDescription = JobDescription(
            name, File(tempDirPath), File(destDirPath),
            threads.value.ifBlank { "0" }.toInt(),
            ram.value.ifBlank { "0" }.toInt(),
            key,
            plotsToFinish.value.ifBlank { "0" }.toInt(),
            kSize.value.ifBlank { "32" }.toInt(),
            additionalParams.value.split(" ")
        )
        return newDescription
    }
}
