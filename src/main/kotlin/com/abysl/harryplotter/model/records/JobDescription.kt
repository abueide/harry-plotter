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

package com.abysl.harryplotter.model.records

import com.abysl.harryplotter.util.FileSerializer
import com.abysl.harryplotter.util.invoke
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import java.io.File

@JvmRecord
@Serializable(with = FileSerializer::class)
data class JobDescription(
    val name: MutableStateFlow<String>,
    val tempDir: MutableStateFlow<File>,
    val destDir: MutableStateFlow<File>,
    val threads: MutableStateFlow<Int>,
    val ram: MutableStateFlow<Int>,
    val key: MutableStateFlow<ChiaKey>,
    val plotsToFinish: MutableStateFlow<Int>, // -1  = keep going forever
) {
    override fun toString(): String {
        return name()
    }
}
