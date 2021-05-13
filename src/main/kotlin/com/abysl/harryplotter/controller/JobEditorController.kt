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
import com.abysl.harryplotter.util.limitToInt
import com.abysl.harryplotter.windows.KeyEditorWindow
import com.abysl.harryplotter.windows.SimpleDialogs
import com.abysl.harryplotter.windows.SimpleFileChooser
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.MultipleSelectionModel
import javafx.scene.control.SingleSelectionModel
import javafx.scene.control.TextField
import java.io.File
import java.net.URL
import java.util.ResourceBundle

class JobEditorController : Initializable {
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

    private lateinit var jobs: ObservableList<JobProcess>
    private lateinit var keys: ObservableList<ChiaKey>
    private lateinit var selectedJob: MultipleSelectionModel<JobProcess?>
    private lateinit var selectedKey: SingleSelectionModel<ChiaKey?>

    private val dialogs: SimpleDialogs = SimpleDialogs()

    private val fileChooser = SimpleFileChooser(jobName, dialogs)

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        selectedKey = keysCombo.selectionModel
        keysCombo.selectionModel.selectFirst()
        threads.limitToInt()
        ram.limitToInt()
        plotsToFinish.limitToInt()
    }

    fun initModel(
        jobs: ObservableList<JobProcess>,
        keys: ObservableList<ChiaKey>,
        selectedJob: MultipleSelectionModel<JobProcess?>
    ): SingleSelectionModel<ChiaKey?> {
        keysCombo.items = keys
        this.jobs = jobs
        this.keys = keys
        this.selectedJob = selectedJob
        return keysCombo.selectionModel
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
        val selected = keysCombo.selectionModel.selectedItem
        KeyEditorWindow(selected).show {
            if (it != null) {
                keysCombo.items.remove(selected)
                keysCombo.items.add(it)
            }
        }
    }

    fun onAdd() {
        KeyEditorWindow().show {
            if (it != null) keysCombo.items.add(it)
        }
    }

    fun onCancel() {
        jobName.clear()
        tempDir.clear()
        destDir.clear()
        threads.clear()
        ram.clear()
        selectedKey.clearSelection()
        selectedJob.clearSelection()
    }

    fun onSave() {
        if (tempDir.text.isBlank() || destDir.text.isBlank()) {
            dialogs.showAlert(
                "Directory Not Selected",
                "Please make sure to select a destination & temporary directory."
            )
            return
        }
        val temp = File(tempDir.text)
        val dest = File(destDir.text)
        if (!temp.exists()) {
            dialogs.showAlert("Temp Directory Does Not Exist", "Please select a valid directory.")
            return
        }
        if (!dest.exists()) {
            dialogs.showAlert("Destination Directory Does Not Exist", "Please select a valid directory.")
            return
        }
        if (!temp.isDirectory) {
            dialogs.showAlert("Selected Temp Is Not A Directory", "Please select a valid directory.")
            return
        }
        if (!dest.isDirectory) {
            dialogs.showAlert("Selected Destination Is Not A Directory", "Please select a valid directory.")
            return
        }
        if (selectedKey.selectedItem == null) {
            dialogs.showAlert("Key Not Selected", "Please add and select a key")
        }

        val name = jobName.text.ifBlank { "Plot Job ${jobs.count() + 1}" }
        jobs.add(
            JobProcess(
                chia,
                JobDescription(
                    name, File(tempDir.text), File(destDir.text),
                    threads.text.ifBlank { "0" }.toInt(),
                    ram.text.ifBlank { "0" }.toInt(),
                    selectedKey.selectedItem!!,
                    plotsToFinish.text.ifBlank { "0" }.toInt()
                )
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
        selectedKey.select(jobDesc.key)
        //TODO: Remove reference to logswindow
//        logsWindow.text = jobProc.getLogsAsString()
        // Makes the textarea scroll to the bottom by default
//        logsWindow.appendText(" ")
    }


}
