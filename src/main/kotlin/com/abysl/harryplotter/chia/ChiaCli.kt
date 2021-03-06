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

package com.abysl.harryplotter.chia

import com.abysl.harryplotter.config.Config
import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.model.jobs.ChiaKey
import com.abysl.harryplotter.model.jobs.JobDescription
import com.abysl.harryplotter.model.jobs.PlotProcess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess

class ChiaCli(val exe: File = File(Prefs.exePath), val config: File = File(Prefs.configPath)) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.JavaFx

    val chiaHome = config.parentFile.parentFile

    init {
        if (!exe.exists()) {
            Prefs.exePath = "NOT FOUND"
            exitProcess(1)
        }
        if (!config.exists()) {
            Prefs.configPath = "NOT FOUND"
            exitProcess(1)
        }
    }

    fun readKeys(): List<ChiaKey> {
        val keyInput = runCommand("keys", "show")
        val keys = mutableListOf<ChiaKey>()
        for (line in keyInput) {
            if (line.contains("Fingerprint")) {
                keys.add(ChiaKey())
            }
            if (keys.isNotEmpty()) {
                keys[keys.size - 1] = keys[keys.size - 1].parseLine(line)
            }
        }
        return keys
    }

    /**
     * Usage: runCommand("keys", "show") -> chia.exe keys show
     * @param args
     * @return
     */

    fun runCommand(vararg commandArgs: String, timeout: Long = 100): List<String> {
        val command: List<String> = listOf(exe.path) + commandArgs.toList()
        val proc: Process = ProcessBuilder(command)
            .start()
        proc.waitFor(timeout, TimeUnit.SECONDS)
        val input: InputStream = proc.inputStream
        return input.reader().readLines()
    }

    fun createPlot(desc: JobDescription, cacheDir: File = desc.destDir, onComplete: () -> Unit): PlotProcess? {
        var counter = 0
        var outputFile = Config.plotLogsRunning.resolve("${desc.name}.log")
        while (outputFile.exists()) {
            counter++
            outputFile = Config.plotLogsRunning.resolve("${desc.name}$counter.log")
        }
        val args = mutableListOf<String>()
        args.addAll(listOf("plots", "create", "-k", desc.kSize.toString()))
        if (desc.key.farmerKey.isNotBlank() && desc.key.poolKey.isNotBlank()) {
            args.addAll(listOf("-f", desc.key.farmerKey, "-c", desc.key.poolKey))
        } else if (desc.key.fingerprint.isNotBlank())
            args.addAll(listOf("-a", desc.key.fingerprint))
        else
            return null
        if (desc.ram >= JobDescription.MINIMUM_RAM) args.addAll(listOf("-b", desc.ram.toString()))
        if (desc.threads > 0) args.addAll(listOf("-r", desc.threads.toString()))
        val destDir = if (desc.useCacheDrive) cacheDir.toString() else desc.destDir.toString()
        args.addAll(listOf("-t", desc.tempDir.toString(), "-d", destDir))
        desc.additionalParams.forEach { if (it.isNotBlank()) args.add(it) }
        val proc = runCommandAsync(outputFile, *args.toTypedArray())
        return PlotProcess(proc.pid(), outputFile).also { it.initialized(onComplete) }
    }

    fun runCommandAsync(
        outputFile: File,
        vararg commandArgs: String,
    ): Process {
        val command: List<String> = listOf(exe.path) + commandArgs.toList()
        val proc: Process = ProcessBuilder(command)
            .redirectOutput(outputFile)
            .redirectError(outputFile)
            .start()
        return proc
    }
}
