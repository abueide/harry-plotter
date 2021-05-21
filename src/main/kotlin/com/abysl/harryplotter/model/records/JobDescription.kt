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

import com.abysl.harryplotter.chia.ChiaCli
import com.abysl.harryplotter.util.serializers.FileSerializer
import kotlinx.serialization.Serializable
import java.io.File

@JvmRecord
@Serializable
data class JobDescription(
    val name: String,
    @Serializable(with = FileSerializer::class)
    val tempDir: File,
    @Serializable(with = FileSerializer::class)
    val destDir: File,
    val threads: Int,
    val ram: Int,
    val key: ChiaKey,
    val plotsToFinish: Int,
    val kSize: Int = 32,
    val additionalParams: List<String> = listOf()
) {

    fun launch(
        chia: ChiaCli,
        ioDelay: Long = 10,
        onOutput: (String) -> Unit,
        onCompleted: (Double) -> Unit
    ): Process {

        val args = mutableListOf<String>()

        args.addAll(listOf("plots", "create", "-k", kSize.toString()))
        if (key.fingerprint.isNotBlank()) args.addAll(listOf("-a", key.fingerprint))
        else if (key.farmerKey.isNotBlank() && key.poolKey.isNotBlank()) {
            args.addAll(listOf("-f", key.farmerKey, "-p", key.poolKey))
        }
        if (ram > MINIMUM_RAM) args.addAll(listOf("-b", ram.toString()))
        if (threads > 0) args.addAll(listOf("-r", threads.toString()))
        args.addAll(listOf("-t", tempDir.toString(), "-d", destDir.toString()))
        additionalParams.forEach { if(it.isNotBlank()) args.add(it) }

        return chia.runCommandAsync(
            ioDelay = ioDelay,
            outputCallback = onOutput,
            completedCallback = onCompleted,
            *args.toTypedArray()
        )
    }

    override fun toString(): String {
        return name
    }

    companion object {
        private const val MINIMUM_RAM = 2500 // MiB
    }
}
