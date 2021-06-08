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

import com.abysl.harryplotter.util.serializers.FileSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.io.File
import java.util.Locale

@Serializable
abstract class Drive {
    abstract val name: String
    abstract val drivePath: File
    abstract val type: DriveType

    override fun toString(): String {
        val freeSpaceFormatted = String.format(Locale.US, "%.2f", getFreeSpaceGiB())
        val totalSpaceFormatted = String.format(Locale.US, "%.2f", getTotalSpaceGiB())
        return "name - $freeSpaceFormatted GiB / $totalSpaceFormatted GiB"
    }
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

    companion object {
        const val BYTES_TO_GB_FACTOR = 1_073_741_824L
    }
//    fun deepCopy(): Drive = Drive(this.name, this.drivePath, this.type, this.staggerSettings)
}
