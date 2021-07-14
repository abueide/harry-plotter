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
sealed class Drive {
    abstract val name: String
    abstract val drivePath: File
    abstract val driveType: DriveType

    abstract fun deepCopy(): Drive

    fun getTotalSpaceGiB(): Double {
        return (drivePath.totalSpace / BYTES_TO_GB_FACTOR).toDouble()
    }

    fun getFreeSpaceGiB(): Double {
        return (drivePath.freeSpace / BYTES_TO_GB_FACTOR).toDouble()
    }

    fun getUsedSpaceGiB(): Double {
        return getTotalSpaceGiB() - getFreeSpaceGiB()
    }

    override fun toString(): String {
        val usedSpace = getUsedSpaceGiB()
        val totalSpace = getTotalSpaceGiB()
        val usedSpaceFormatted = String.format(Locale.US, "%.2f", usedSpace)
        val totalSpaceFormatted = String.format(Locale.US, "%.2f", totalSpace)
        val percentageUsed = (usedSpace / totalSpace * 100).toInt()
        return "$name ($percentageUsed%) - $usedSpaceFormatted GiB / $totalSpaceFormatted GiB"
    }

    companion object {
        const val BYTES_TO_GB_FACTOR = 1_073_741_824L
    }
}
