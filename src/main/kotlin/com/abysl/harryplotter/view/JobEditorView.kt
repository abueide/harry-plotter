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

package com.abysl.harryplotter.view

import com.abysl.harryplotter.model.records.ChiaKey
import com.abysl.harryplotter.util.bindings.bindBidirectional
import com.abysl.harryplotter.util.invoke
import com.abysl.harryplotter.util.limitToInt
import com.abysl.harryplotter.viewmodel.JobEditorViewModel
import com.abysl.harryplotter.windows.SimpleFileChooser
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File

class JobEditorView {
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

    @FXML
    private lateinit var kSize: TextField

    @FXML
    private lateinit var additionalParams: TextField

    private lateinit var fileChooser: SimpleFileChooser

    lateinit var viewModel: JobEditorViewModel

    fun initialized(jobEditorViewModel: JobEditorViewModel) {
        this.viewModel = jobEditorViewModel

        fileChooser = SimpleFileChooser(jobName)

        threads.limitToInt()
        ram.limitToInt()
        plotsToFinish.limitToInt()
        kSize.limitToInt()

        // Bind JobDescription to viewmodel bidirectionally
        viewModel.jobName.bindBidirectional(jobName.textProperty())
        viewModel.tempDir.bindBidirectional(tempDir.textProperty())
        viewModel.destDir.bindBidirectional(destDir.textProperty())
        viewModel.threads.bindBidirectional(threads.textProperty())
        viewModel.ram.bindBidirectional(ram.textProperty())
        viewModel.plotsToFinish.bindBidirectional(plotsToFinish.textProperty())
        viewModel.kSize.bindBidirectional(kSize.textProperty())
        viewModel.additionalParams.bindBidirectional(additionalParams.textProperty())
        viewModel.stopAfterCheck.bindBidirectional(stopAfterCheck.selectedProperty())
        viewModel.stopAfterCheck.onEach {
            plotsToFinish.disableProperty().set(!it)
        }.launchIn(CoroutineScope(Dispatchers.IO))

        // Bind Chia Keys Combo Box Items to ViewModel
        viewModel.chiaKeys.onEach {
            Platform.runLater { keysCombo.items = FXCollections.observableList(it) }
        }.launchIn(CoroutineScope(Dispatchers.IO))

        // Bind selected key to viewmodel bidirectionally
        keysCombo.selectionModel.select(viewModel.selectedKey())
        keysCombo.selectionModel.selectedItemProperty().addListener { observable, old, new ->
            if (old != new) {
                jobEditorViewModel.selectedKey.value = new
            }
        }
        jobEditorViewModel.selectedKey.onEach {
            Platform.runLater {
                if (it == null) {
                    keysCombo.selectionModel.clearSelection()
                } else {
                    keysCombo.selectionModel.select(it)
                }
            }
        }.launchIn(CoroutineScope(Dispatchers.IO))
    }

    fun onTempBrowse() {
        val startingPath = viewModel.tempDir.get()
        val startingFile = if(File(startingPath).exists()) File(startingPath) else null
        fileChooser.chooseDir("Select Temp Dir", false, startingFile)?.let {
            viewModel.tempDir.value = it.absolutePath
        }
    }

    fun onDestBrowse() {
        val startingPath = viewModel.tempDir.get()
        val startingFile = if(File(startingPath).exists()) File(startingPath) else null
        fileChooser.chooseDir("Select Destination Dir", false, startingFile)?.let {
            viewModel.destDir.value = it.absolutePath
        }
    }

    fun onEdit() {
        viewModel.onEdit()
    }

    fun onAdd() {
        viewModel.onAdd()
    }

    fun onNew() {
        viewModel.onNew()
    }

    fun onCancel() {
        viewModel.onCancel()
    }

    fun onSave() {
        viewModel.onSave()
    }
}
