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

package com.abysl.harryplotter.model.jobs

import com.abysl.harryplotter.util.serializers.FileSerializer
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class JobDescription(
    val name: String,
    @Serializable(with = FileSerializer::class)
    val tempDir: File,
    @Serializable(with = FileSerializer::class)
    val destDir: File,
    val useCacheDrive: Boolean = false,
    val threads: Int,
    val ram: Int,
    val key: ChiaKey,
    val plotsToFinish: Int,
    val kSize: Int = 32,
    val additionalParams: List<String> = listOf()
) {
    override fun toString(): String {
        return name
    }

    companion object {
        const val MINIMUM_RAM = 900 // MiB
    }
}
