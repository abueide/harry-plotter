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

package com.abysl.harryplotter.controller

import com.abysl.harryplotter.chia.ChiaCli
import com.abysl.harryplotter.data.ChiaKey
import com.abysl.harryplotter.data.JobDescription
import com.abysl.harryplotter.data.JobProcess
import com.abysl.harryplotter.model.DataModel.jobs
import com.abysl.harryplotter.model.DataModel.keys
import com.abysl.harryplotter.model.DataModel.keysFlow
import com.abysl.harryplotter.model.DataModel.selectedJob
import com.abysl.harryplotter.model.DataModel.selectedKey
import com.abysl.harryplotter.model.DataModel.selectedKeyFlow
import com.abysl.harryplotter.util.limitToInt
import com.abysl.harryplotter.windows.KeyEditorWindow
import com.abysl.harryplotter.windows.SimpleDialogs.showAlert
import com.abysl.harryplotter.windows.SimpleFileChooser
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File

class JobEditorController {
    @FXML
    private lateinit var jobName: TextField

    @FXML
    private lateinit var tempDir: TextField

    @FXML
    private lateinit var destDir: TextField

    @FXML
    private lateinit var threads: TextField

    @FXML
    private lateinit var ram: TextField

    @FXML
    private lateinit var keysCombo: ComboBox<ChiaKey>

    @FXML
    private lateinit var stopAfterCheck: CheckBox

    @FXML
    private lateinit var plotsToFinish: TextField

    lateinit var chia: ChiaCli

    private lateinit var fileChooser: SimpleFileChooser

    fun initialized() {
        // Bind selected key bi directionally with the listeners
        keysFlow
            .onEach { Platform.runLater {
                keysCombo.items.setAll(it)
                keysCombo.selectionModel.select(selectedKey)
            } }
            .launchIn(CoroutineScope(Dispatchers.IO))
        selectedKeyFlow
            .onEach { key ->
                Platform.runLater {
                    key?.let { keysCombo.selectionModel.select(key) }
                }
            }
            .launchIn(CoroutineScope(Dispatchers.IO))
        keysCombo.selectionModel.selectedItemProperty().addListener { observable, old, new ->
            new?.let { selectedKey = new }
        }
        fileChooser = SimpleFileChooser(jobName)
        // Allow user input of only integers
        threads.limitToInt()
        ram.limitToInt()
        plotsToFinish.limitToInt()
    }

    fun onStopAfter() {
        plotsToFinish.disableProperty().value = !stopAfterCheck.selectedProperty().value
        plotsToFinish.text = ""
    }

    fun onTempBrowse() {
        fileChooser.chooseDir("Select Temp Dir", false)?.let {
            tempDir.text = it.absolutePath
        }
    }

    fun onDestBrowse() {
        fileChooser.chooseDir("Select Destination Dir", false)?.let {
            destDir.text = it.absolutePath
        }
    }

    fun onEdit() {
        val oldKey = selectedKey ?: return
        KeyEditorWindow(oldKey).show { newKey ->
            if (newKey != null) {
                var test = keys
                keys -= oldKey
                test = keys
                keys += newKey
                test = keys
                selectedKey = newKey
                val test2 = selectedKey
            }
        }
    }

    fun onAdd() {
        KeyEditorWindow().show {
            if (it != null) keys += it
        }
    }

    fun onCancel() {
        jobName.clear()
        tempDir.clear()
        destDir.clear()
        threads.clear()
        ram.clear()
        selectedKey = null
        selectedJob = null
    }

    fun onSave() {
        if (tempDir.text.isBlank() || destDir.text.isBlank()) {
            showAlert(
                "Directory Not Selected",
                "Please make sure to select a destination & temporary directory."
            )
            return
        }
        val temp = File(tempDir.text)
        val dest = File(destDir.text)
        if (!temp.exists()) {
            showAlert("Temp Directory Does Not Exist", "Please select a valid directory.")
            return
        }
        if (!dest.exists()) {
            showAlert("Destination Directory Does Not Exist", "Please select a valid directory.")
            return
        }
        if (!temp.isDirectory) {
            showAlert("Selected Temp Is Not A Directory", "Please select a valid directory.")
            return
        }
        if (!dest.isDirectory) {
            showAlert("Selected Destination Is Not A Directory", "Please select a valid directory.")
            return
        }
        if (selectedKey == null) {
            showAlert("Key Not Selected", "Please add and select a key")
        }
        val key = selectedKey ?: return
        val name = jobName.text.ifBlank { "Plot Job ${jobs.count() + 1}" }
        jobs += JobProcess(
            chia,
            JobDescription(
                name, File(tempDir.text), File(destDir.text),
                threads.text.ifBlank { "0" }.toInt(),
                ram.text.ifBlank { "0" }.toInt(),
                key,
                plotsToFinish.text.ifBlank { "0" }.toInt()
            )
        )

    }

    fun loadJob(jobProc: JobProcess) {
        val jobDesc = jobProc.jobDesc
        jobName.text = jobDesc.name
        tempDir.text = jobDesc.tempDir.path
        destDir.text = jobDesc.destDir.path
        threads.text = jobDesc.threads.toString()
        ram.text = jobDesc.ram.toString()
        plotsToFinish.text = jobDesc.plotsToFinish.toString()
        selectedKey = jobDesc.key
    }
}
