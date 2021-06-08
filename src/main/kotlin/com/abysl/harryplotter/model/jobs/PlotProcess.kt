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

package com.abysl.harryplotter.model.jobs

import com.abysl.harryplotter.config.Config
import com.abysl.harryplotter.util.IOUtil
import com.abysl.harryplotter.util.invoke
import com.abysl.harryplotter.util.serializers.FileSerializer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime

@Serializable
class PlotProcess(
    val pid: Long,
    val logFile: File,
) {

    @Transient
    val state: MutableStateFlow<JobState> = MutableStateFlow(
        JobState(
            running = isRunning(),
            currentResult = JobResult(timeStarted = Clock.System.now())
        )
    )

    @Transient
    private val _newLogs = MutableStateFlow<List<String>>(listOf())

    @Transient
    val newLogs = _newLogs.asStateFlow()

    @Transient
    lateinit var onComplete: () -> Unit

    @Transient
    var oldLogCounter = 0

    private val process: ProcessHandle?
        get() = ProcessHandle.of(pid).orElseGet { null }

    fun initialized(onComplete: () -> Unit) {
        this.onComplete = onComplete
        if (logFile.exists()) {
            state.value = state.value.parseAll(logFile.readLines())
        }
        if (!isRunning()) onComplete()
    }

    suspend fun update(delay: Long) = coroutineScope {
        while (isRunning()) {
            updateLogs()
            delay(delay)
        }
        updateLogs()
        onComplete()
    }

    private fun updateLogs() {
        var lineNum = 0
        var tempCache = listOf<String>()
        if (logFile.exists()) {
            logFile.forEachLine {
                if (lineNum >= oldLogCounter) {
                    tempCache += it
                    oldLogCounter++
                }
                lineNum++
            }
        }
        state.value = state.value.parseAll(tempCache).copy(running = isRunning())
        _newLogs.value = tempCache
    }

    fun isRunning(): Boolean {
        val proc = process
        return proc != null && proc.isAlive && logFile.exists()
    }

    fun kill() {
        process?.destroyForcibly()
    }

    @OptIn(ExperimentalTime::class)
    fun timeRunning(): Double {
        val timeRunning = Clock.System.now() - (state().currentResult.timeStarted ?: return 0.0)
        return timeRunning.toDouble(TimeUnit.SECONDS)
    }

    fun dispose() {
        val updatedState = state.value
        kill()
        if (updatedState.completed) {
            moveTo(logFile, Config.plotLogsFinished.resolve("log-${state.value.plotId}.log"))
        } else {
            moveTo(logFile, Config.plotLogsFailed.resolve("log-${state.value.plotId}.log"))
        }
    }

    fun readLogs(): String {
        return if (logFile.exists()) logFile.readText() else ""
    }

    private fun moveTo(file: File, destination: File) {
        try {
            if (!file.exists()) return
            file.copyTo(destination, true)
            IOUtil.deleteFile(file)
        } catch (e: Exception) {
            println("Log Copying error")
            e.printStackTrace()
        }
    }
}
