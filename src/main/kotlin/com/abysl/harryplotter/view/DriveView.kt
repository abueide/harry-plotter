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

import com.abysl.harryplotter.config.Config
import com.abysl.harryplotter.model.DriveType
import com.abysl.harryplotter.model.PlotJob
import com.abysl.harryplotter.model.records.Drive
import com.abysl.harryplotter.util.invoke
import com.abysl.harryplotter.util.limitToInt
import com.abysl.harryplotter.viewmodel.DriveViewModel
import com.abysl.harryplotter.windows.SimpleDialogs
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListView
import javafx.scene.control.MenuItem
import javafx.scene.control.TextField
import java.net.URL
import java.util.ResourceBundle

class DriveView: Initializable {

    @FXML
    lateinit var driveList: ListView<Drive>

    @FXML
    lateinit var driveName: TextField

    @FXML
    lateinit var drivePath: TextField

    @FXML
    lateinit var driveTypes: ComboBox<DriveType>

    @FXML
    lateinit var staticStagger: TextField

    @FXML
    lateinit var staticIgnore: CheckBox

    @FXML
    lateinit var maxP1: TextField

    @FXML
    lateinit var maxOther: TextField

    @FXML
    lateinit var maxConcurrent: TextField

    lateinit var viewModel: DriveViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        driveTypes.items.addAll(DriveType.values())
        driveTypes.selectionModel.selectFirst()

    }

    fun initialized(viewModel: DriveViewModel){
        this.viewModel = viewModel
        staticStagger.limitToInt()
        maxP1.limitToInt()
        maxOther.limitToInt()
        maxConcurrent.limitToInt()
        driveName.textProperty().bindBidirectional(viewModel.driveName)
        drivePath.textProperty().bindBidirectional(viewModel.drivePath)

        driveTypes.selectionModel.selectedItemProperty().addListener { _, _, new -> viewModel.driveType.set(new) }
        viewModel.driveType.addListener { _, _, new -> driveTypes.selectionModel.select(new) }
        staticStagger.textProperty().bindBidirectional(viewModel.staticStagger)
        maxP1.textProperty().bindBidirectional(viewModel.maxP1)
        maxOther.textProperty().bindBidirectional(viewModel.maxOther)
        maxConcurrent.textProperty().bindBidirectional(viewModel.maxConcurrent)

        driveList.items = viewModel.drives
        driveList.selectionModel.selectedItemProperty().addListener { _, old, new ->
            if(old != new) viewModel.selectedDrive.set(new)
        }
        viewModel.selectedDrive.addListener { _, old, new ->
            if(old != new) driveList.selectionModel.select(new)
        }
        driveList.contextMenu = drivesMenu

        staticIgnore.selectedProperty().bindBidirectional(viewModel.ignoreStatic)
    }



    fun onNew(){
        viewModel.new()
    }

    fun onCancel(){
        viewModel.cancel()
    }

    fun onSave(){
        viewModel.save()
        driveList.refresh()
    }

    val drivesMenu = ContextMenu()

    val duplicate = MenuItem("Duplicate").also {
        it.setOnAction {
            val drive = viewModel.selectedDrive.get() ?: return@setOnAction
            viewModel.drives.add(drive.deepCopy())
            Config.saveDrives(viewModel.drives)
        }
        drivesMenu.items.add(it)
    }

    val delete = MenuItem("Delete").also {
        it.setOnAction {
            val drive = viewModel.selectedDrive.get() ?: return@setOnAction
            if (SimpleDialogs.showConfirmation("Delete Job?", "Are you sure you want to delete ${drive.name}")) {
                viewModel.drives.remove(drive)
                Config.saveDrives(viewModel.drives)
            }
        }
        drivesMenu.items.add(it)
    }
}