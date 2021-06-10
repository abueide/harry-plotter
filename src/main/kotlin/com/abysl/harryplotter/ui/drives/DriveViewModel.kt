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
import com.abysl.harryplotter.util.invoke
import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.lang.NullPointerException

class DriveViewModel {

    val drives = MutableStateFlow<List<Drive>>(emptyList())
    val selectedDrive = SimpleObjectProperty<Drive?>(null)

    fun newDrive(type: DriveType): Drive {
        val drive = when (type) {
            DriveType.TEMP -> TempDrive()
            DriveType.DESTINATION -> DestDrive()
            DriveType.CACHE -> CacheDrive()
        }
        drives.value += drive
        selectedDrive.set(drive)
        Config.saveDrives(drives())
        return drive
    }

    fun cancel() {
        selectedDrive.set(null)
    }

    fun save(drive: Drive) {
        val selected = selectedDrive.get() ?: newDrive(drive.driveType)
        val drivesList = drives().toMutableList()
        val index = drivesList.indexOf(selected)
        if(index != -1) {
            drivesList[index] = drive
            drives.value = drivesList.toList()
            selectedDrive.set(drivesList[index])
            Config.saveDrives(drives.value)
        }else {
            println("Selected drive not found in drives list!")
        }
    }
}
