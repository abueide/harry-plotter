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

@file:UseSerializers(FileSerializer::class)
package com.abysl.harryplotter.model.records

import com.abysl.harryplotter.model.DriveType
import com.abysl.harryplotter.util.serializers.FileSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.io.File

@Serializable
class Drive(
    val name: String = "",
    val drivePath: File = File(""),
    val type: DriveType = DriveType.TEMP,
    val staggerSettings: StaggerSettings = StaggerSettings()
) {
    override fun toString(): String {
        return name
    }

    fun deepCopy(): Drive = Drive(this.name, this.drivePath, this.type, this.staggerSettings)
}
