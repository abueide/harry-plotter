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
import com.abysl.harryplotter.model.DriveType
import com.abysl.harryplotter.model.records.Drive
import com.abysl.harryplotter.model.records.StaggerSettings
import com.abysl.harryplotter.windows.SimpleDialogs
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import java.io.File

class DriveViewModel {

    val drives = FXCollections.observableArrayList<Drive>()

    val selectedDrive = SimpleObjectProperty<Drive?>(null)

    val driveName = SimpleStringProperty("")
    val drivePath = SimpleStringProperty("")
    val driveType = SimpleObjectProperty(DriveType.TEMP)
    val maxP1 = SimpleStringProperty("")
    val maxOther = SimpleStringProperty("")
    val maxConcurrent = SimpleStringProperty("")
    val staticStagger = SimpleStringProperty("")
    val ignoreStatic = SimpleBooleanProperty(false)

    init {
        selectedDrive.addListener { _, _, new -> loadDrive(new) }
    }

    fun new(){
        val drive = Drive(name = "Unnamed Drive")
        drives.add(drive)
        selectedDrive.set(drive)
        Config.saveDrives(drives)
    }

    fun cancel(){
        selectedDrive.set(null)
    }

    fun save(){
        val drive = selectedDrive.get()
        if(drive == null){
            drives.add(getDrive())
        }else{
            val index = drives.indexOf(drive)
            drives[index] = getDrive()
        }
        Config.saveDrives(drives)
        selectedDrive.set(drive)
    }

    fun loadDrive(someDrive: Drive?){
        val drive = someDrive ?: Drive()
        driveName.set(drive.name)
        drivePath.set(drive.drivePath.path)
        driveType.set(drive.type)
        loadStagger(drive.staggerSettings)
    }

    fun loadStagger(staggerSettings: StaggerSettings){
        maxP1.set(staggerSettings.maxFirstStagger.toString())
        maxOther.set(staggerSettings.maxOtherStagger.toString())
        maxConcurrent.set(staggerSettings.maxTotal.toString())
        staticStagger.set(staggerSettings.staticStagger.toString())
        ignoreStatic.set(staggerSettings.ignoreStatic)
    }

    fun getDrive(): Drive {
        val driveFolder = File(drivePath.get())
        if (!driveFolder.exists()) {
            SimpleDialogs.showAlert(
                "Drive Path Not Selected",
                "Please select the folder where your drive is mounted"
            )
        }
        return Drive(
            driveName.get(),
            driveFolder,
            driveType.get(),
            getStagger()
        )
    }

    fun getStagger(): StaggerSettings {
        return StaggerSettings(
            maxP1.get().ifBlank { "0" }.toInt(),
            maxOther.get().ifBlank { "0" }.toInt(),
            maxConcurrent.get().ifBlank { "0" }.toInt(),
            staticStagger.get().ifBlank { "0" }.toInt(),
            ignoreStatic.get()
        )
    }
}