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
import com.abysl.harryplotter.util.bind
import com.abysl.harryplotter.util.bindBidirectional
import com.abysl.harryplotter.util.bindings.bind
import com.abysl.harryplotter.util.bindings.bindBidirectional
import com.abysl.harryplotter.util.limitToInt
import com.abysl.harryplotter.viewmodel.JobEditorViewModel
import com.abysl.harryplotter.viewmodel.MainViewModel
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

    private lateinit var fileChooser: SimpleFileChooser
    lateinit var viewModel: JobEditorViewModel
    lateinit var mainViewModel: MainViewModel

    fun initialized(viewModel: JobEditorViewModel) {
        this.viewModel = viewModel
        this.mainViewModel = viewModel.mainViewModel

        fileChooser = SimpleFileChooser(jobName)

        threads.limitToInt()
        ram.limitToInt()
        plotsToFinish.limitToInt()

        viewModel.jobName.bindBidirectional(jobName.textProperty())
        viewModel.tempDir.bindBidirectional(tempDir.textProperty())
        viewModel.destDir.bindBidirectional(destDir.textProperty())
        viewModel.threads.bindBidirectional(threads.textProperty())
        viewModel.ram.bindBidirectional(ram.textProperty())
        viewModel.plotsToFinish.bindBidirectional(plotsToFinish.textProperty())

        viewModel.mainViewModel.chiaKeys.bind(keysCombo.itemsProperty().get())
        mainViewModel.selectedKey.bind(keysCombo.selectionModel.selectedItemProperty())
        mainViewModel.selectedKey.onEach {
            Platform.runLater {
                if (it == null) {
                    keysCombo.selectionModel.clearSelection()
                } else if (it in keysCombo.items) {
                    keysCombo.selectionModel.select(it)
                }
            }
        }.launchIn(CoroutineScope(Dispatchers.IO))
    }

    fun onStopAfter() {
        plotsToFinish.disableProperty().value = !stopAfterCheck.selectedProperty().value
        plotsToFinish.clear()
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
        viewModel.onEdit()
    }

    fun onAdd() {
        viewModel.onAdd()
    }

    fun onCancel() {
        viewModel.onCancel()
    }

    fun onSave() {
        viewModel.onSave()
    }
}