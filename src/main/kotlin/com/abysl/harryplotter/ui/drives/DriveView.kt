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

package com.abysl.harryplotter.ui.drives

import com.abysl.harryplotter.config.Config
import com.abysl.harryplotter.model.drives.CacheDrive
import com.abysl.harryplotter.model.drives.DestDrive
import com.abysl.harryplotter.model.drives.Drive
import com.abysl.harryplotter.model.drives.DriveType
import com.abysl.harryplotter.model.drives.TempDrive
import com.abysl.harryplotter.ui.all.SimpleDialogs
import com.abysl.harryplotter.ui.all.SimpleFileChooser
import com.abysl.harryplotter.util.getResource
import com.abysl.harryplotter.util.invoke
import com.abysl.harryplotter.util.limitToInt
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListView
import javafx.scene.control.MenuItem
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File
import java.net.URL
import java.util.ResourceBundle

class DriveView : Initializable {

    @FXML
    lateinit var driveList: ListView<Drive>

    @FXML
    lateinit var driveBox: VBox

    @FXML
    lateinit var driveName: TextField

    @FXML
    lateinit var drivePath: TextField

    @FXML
    lateinit var driveTypes: ComboBox<DriveType>



    lateinit var viewModel: DriveViewModel

    val tempDriveLoader = FXMLLoader("ui/drives/TempDriveView.fxml".getResource()).also { it.load() }
    val tempDriveView = tempDriveLoader.getRoot<Node>()
    val tempDriveController = tempDriveLoader.getController<TempDriveView>()

    val destDriveLoader = FXMLLoader("ui/drives/DestDriveView.fxml".getResource()).also { it.load() }
    val destDriveView = destDriveLoader.getRoot<Node>()
    val destDriveController = destDriveLoader.getController<DestDriveView>()


    val cacheDriveLoader = FXMLLoader("ui/drives/CacheDriveView.fxml".getResource()).also { it.load() }
    val cacheDriveView = destDriveLoader.getRoot<Node>()
    val cacheDriveController = destDriveLoader.getController<CacheDriveView>()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        driveTypes.items.addAll(DriveType.values())
        driveTypes.selectionModel.selectFirst()
    }

    fun initialized(viewModel: DriveViewModel) {
        this.viewModel = viewModel

        viewModel.selectedDrive.addListener { _, _, new -> loadDrive(new) }
        driveName.textProperty().bindBidirectional(viewModel.driveName)
        drivePath.textProperty().bindBidirectional(viewModel.drivePath)
        driveTypes.selectionModel.selectedItemProperty().addListener { _, _, new -> viewModel.driveType.set(new) }
        viewModel.driveType.addListener { _, _, new -> driveTypes.selectionModel.select(new) }

        viewModel.drives.onEach { drives ->
            Platform.runLater {
                driveList.items.setAll(drives)
                driveList.selectionModel.select(viewModel.selectedDrive.get())
            }
        }.launchIn(CoroutineScope(Dispatchers.IO))
        driveList.selectionModel.selectedItemProperty().addListener { _, old, new ->
            if (old != new) viewModel.selectedDrive.set(new)
        }
        viewModel.selectedDrive.addListener { _, old, new ->
            if (old != new) driveList.selectionModel.select(new)
        }
        driveList.contextMenu = drivesMenu
    }

    fun onBrowse() {
        val startingPath = viewModel.drivePath.get()
        val startingFile = if (File(startingPath).exists()) File(startingPath) else null
        SimpleFileChooser(drivePath).chooseDir("Select Drive Dir", false, startingFile)?.let {
            viewModel.drivePath.set(it.absolutePath)
        }
    }

    fun onNew() {
        viewModel.newDrive(driveTypes.selectionModel.selectedItem!!)
    }

    fun onCancel() {
        viewModel.cancel()
    }

    fun onSave() {
        viewModel.save(getDrive())
    }


    private fun loadDrive(drive: Drive?) {
        when (drive) {
            null -> return
            is TempDrive -> loadTempDrive(drive)
            is CacheDrive -> loadCacheDrive(drive)
            is DestDrive -> loadDestDrive(drive)
            else -> throw IllegalArgumentException("Drive View not defined for drive type ${drive::class.qualifiedName}")
        }
    }


    private fun loadTempDrive(drive: TempDrive) {
        driveBox.children[1] = tempDriveView
        tempDriveController.loadDrive(drive)
    }

    private fun loadDestDrive(drive: DestDrive) {
        driveBox.children[1] = destDriveView
        destDriveController.loadDrive(drive)
    }

    private fun loadCacheDrive(drive: CacheDrive) {
        driveBox.children[1] = cacheDriveView
        cacheDriveController.loadDrive(drive)
    }

    private fun getDrive(): Drive {
        val name = driveName.text
        val drivePath = File(drivePath.text)
        val drive = when (driveTypes.selectionModel.selectedItem!!) {
            DriveType.TEMP -> TempDrive(name, drivePath, staggerSettings = tempDriveController.getStagger())
            DriveType.DESTINATION -> DestDrive(
                name,
                drivePath,
                maxPlotTransfer = destDriveController.maxPlotTransfer.text.toInt()
            )
            DriveType.CACHE -> CacheDrive(name, drivePath)
        }
        return drive
    }

    val drivesMenu = ContextMenu()

    val duplicate = MenuItem("Duplicate").also {
        it.setOnAction {
            val drive = viewModel.selectedDrive.get() ?: return@setOnAction
            viewModel.drives.value += (drive.deepCopy())
            Config.saveDrives(viewModel.drives())
        }
        drivesMenu.items.add(it)
    }

    val delete = MenuItem("Delete").also {
        it.setOnAction {
            val drive = viewModel.selectedDrive.get() ?: return@setOnAction
            if (SimpleDialogs.showConfirmation("Delete Job?", "Are you sure you want to delete ${drive.name}")) {
                viewModel.drives.value -= (drive)
                Config.saveDrives(viewModel.drives())
            }
        }
        drivesMenu.items.add(it)
    }
}
