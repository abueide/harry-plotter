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

package com.abysl.harryplotter.model.drives

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import java.io.File

private const val REFRESH_DELAY = 1000L

class CacheManager(val drives: MutableStateFlow<List<Drive>>) {
    private var managerScope = CoroutineScope(Dispatchers.IO)
    private val transferJobs = MutableStateFlow(emptyMap<DestDrive, List<CoroutineScope>>())

    fun start() {
        cancelManager()
        managerScope.launch {
            while (true) {
                val transferMap = transferJobs.value
                getDestDrives()
                    .filter { transferMap.getOrDefault(it, emptyList()).size < it.maxPlotTransfer }
                    .forEach { startTransfer(it) }
                delay(REFRESH_DELAY)
            }
        }
    }

    fun stop() {
        cancelManager()
    }

    private fun startTransfer(drive: DestDrive) {
        val plot = getCacheDrives().flatMap { it.getPlotFiles() }.firstOrNull() ?: return
        if (drive.drivePath.freeSpace < plot.length()) {
            println("Not enough space on destination drive, skipping")
            return
        }
        val renamedPlot = File("${plot.parentFile.path}/.${plot.name}")
        println("Moving ${plot.name} to ${drive.drivePath}")
        try {
            val scope = CoroutineScope(Dispatchers.IO)
            var transfers = transferJobs.value[drive] ?: emptyList()
            transferJobs.value += drive to (transfers + scope)
            FileUtils.moveFile(plot, renamedPlot)
            scope.launch {
                try {
                    FileUtils.moveFile(renamedPlot, File(drive.drivePath, plot.name))
                    transfers = transferJobs.value[drive] ?: emptyList()
                    transferJobs.value += drive to transfers - scope
                } catch (e: Exception) {
                    println("Failed to transfer file from cache drive")
                    println("File: ${plot.path}")
                }
            }
        } catch (e: Exception) {
            println("Failed to transfer file from cache drive")
            println("File: ${plot.path}")
        }
    }

    private fun getCacheDrives(): List<CacheDrive> {
        return drives.value.filterIsInstance<CacheDrive>()
    }

    private fun getDestDrives(): List<DestDrive> {
        return drives.value.filterIsInstance<DestDrive>()
    }

    private fun cancelManager() {
        managerScope.cancel()
        managerScope = CoroutineScope(Dispatchers.IO)
    }
}
