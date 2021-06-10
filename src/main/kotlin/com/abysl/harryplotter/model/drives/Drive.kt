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

package com.abysl.harryplotter.model.drives

import com.abysl.harryplotter.model.StaggerSettings
import com.abysl.harryplotter.util.serializers.FileSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import java.io.File
import java.util.Locale

@Serializable
sealed class Drive {
    abstract val name: String
    abstract val drivePath: File
    abstract val type: DriveType

    abstract fun deepCopy(): Drive

    fun getTotalSpaceGiB(): Double {
        return (drivePath.totalSpace / BYTES_TO_GB_FACTOR).toDouble()
    }

    fun getFreeSpaceGiB(): Double {
        return (drivePath.freeSpace / BYTES_TO_GB_FACTOR).toDouble()
    }

    fun getTotalPlots(){

    }

    fun getHoldablePlots(k: Int = 32){

    }

    override fun toString(): String {
        val freeSpaceFormatted = String.format(Locale.US, "%.2f", getFreeSpaceGiB())
        val totalSpaceFormatted = String.format(Locale.US, "%.2f", getTotalSpaceGiB())
        return "$name - $freeSpaceFormatted GiB / $totalSpaceFormatted GiB"
    }

    companion object {
        const val BYTES_TO_GB_FACTOR = 1_073_741_824L
    }
}

@Serializable
class CacheDrive(
    override val name: String = "Unnamed Cache Drive",
    override val drivePath: File = File(""),
    override val type: DriveType = DriveType.CACHE
) : Drive() {

    override fun deepCopy(): Drive {
        return CacheDrive(name, drivePath, type)
    }
}

@Serializable
class DestDrive(
    override val name: String = "Unnamed Destination Drive",
    override val drivePath: File = File(""),
    override val type: DriveType = DriveType.DESTINATION,
    val maxPlotTransfer: Int = 1
) : Drive() {

    override fun deepCopy(): Drive = DestDrive(name, drivePath, type, maxPlotTransfer)
}

@Serializable
class TempDrive(
    override val name: String = "Unnamed Temp Drive",
    override val drivePath: File = File(""),
    @Transient
    override val type: DriveType = DriveType.TEMP,
    val staggerSettings: StaggerSettings = StaggerSettings(),
): Drive() {
    override fun deepCopy(): Drive = TempDrive(name, drivePath, type, staggerSettings)
}