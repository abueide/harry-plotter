@file:UseSerializers(FileSerializer::class)

package com.abysl.harryplotter.model

import com.abysl.harryplotter.config.Config
import com.abysl.harryplotter.util.invoke
import com.abysl.harryplotter.util.serializers.FileSerializer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime

@Serializable
class PlotProcess(
    val pid: Long,
    val startTime: Instant = Clock.System.now(),
    val logFile: File,
) {

    @Transient
    private val _state: MutableStateFlow<JobState> = MutableStateFlow(JobState(running = isRunning()))

    @Transient
    val state: StateFlow<JobState> = _state.asStateFlow()

    @Transient
    private val _newLogs = MutableStateFlow<List<String>>(listOf())

    @Transient
    val newLogs = _newLogs.asStateFlow()

    @Transient
    private val process
        get() = ProcessHandle.of(pid).orElseGet { null }

    @Transient
    lateinit var onComplete: () -> Unit

    fun initialized(onComplete: () -> Unit) {
        this.onComplete = onComplete
        if (logFile.exists()) {
            _state.value = state.value.parseAll(logFile.readLines())
        }
        if (!isRunning()) onComplete()
    }

    suspend fun update(delay: Long) = coroutineScope {
        var oldLogCounter = 0
        while (true) {
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
            _state.value = _state.value.parseAll(tempCache).copy(running = isRunning())
            _newLogs.value = tempCache
            if (!state().running) {
                onComplete()
            }
            delay(delay)
        }
    }

    fun isRunning(): Boolean {
        return process != null && process.isAlive && logFile.exists()
    }

    fun kill() {
        process?.destroyForcibly()
    }

    @OptIn(ExperimentalTime::class)
    fun timeRunning(): Double {
        val timeRunning = Clock.System.now() - startTime
        return timeRunning.toDouble(TimeUnit.SECONDS)
    }

    fun dispose() {
        val updatedState = state.value
        if (updatedState.completed) {
            moveTo(logFile, Config.plotLogsFinished.resolve(logFile.name))
        } else {
            moveTo(logFile, Config.plotLogsFailed.resolve("log${state.value.plotId}.log"))
        }
        kill()
    }

    fun moveTo(file: File, destination: File): Boolean {
        if (!file.exists()) return false
        destination.delete()
        file.copyTo(destination)
        return file.delete()
    }
}
