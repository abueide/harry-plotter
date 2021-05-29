@file:UseSerializers(FileSerializer::class)

package com.abysl.harryplotter.model

import com.abysl.harryplotter.config.Config
import com.abysl.harryplotter.util.invoke
import com.abysl.harryplotter.util.serializers.FileSerializer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime

private const val LOG_REFRESH_RATE = 100L

@Serializable
class PlotProcess(
    val pid: Long,
    val startTime: Instant = Clock.System.now(),
    val logFile: File,
    val errFile: File,
    @Transient
    val onComplete: () -> Unit = {}
) {

    @Transient
    private var _cache = false

    @Transient
    private var _logCache: MutableStateFlow<List<String>> = MutableStateFlow(listOf())

    @Transient
    private var newLogCounter = 0

    @Transient
    private var logCacheCounter = 0

    @Transient
    private val _state: MutableStateFlow<JobState> = MutableStateFlow(JobState(running = isRunning()))

    @Transient
    private val _newLogs: MutableStateFlow<List<String>> = MutableStateFlow(listOf())

    @Transient
    val state: StateFlow<JobState> = _state.asStateFlow()

    @Transient
    val newLogs = _newLogs.asStateFlow()


    @Transient
    private val process
        get() = ProcessHandle.of(pid).orElseGet { null }

    @Transient
    var cache: Boolean
        get() = _cache
        set(value) {
            if (!value) {
                _logCache.value = listOf()
                logCacheCounter = 0
            }
            _cache = value
        }

    @Transient
    private val logFlow: Flow<List<String>> = flow {
        var lineNum = 0
        logFile.forEachLine {
            if (lineNum >= newLogCounter) {
                _newLogs.value = _newLogs.value + it
                newLogCounter++
            }
            if (lineNum >= logCacheCounter) {
                _logCache.value += it
                logCacheCounter++
            }
            lineNum++
        }
        emit(newLogs())
    }

    suspend fun update(delay: Long) = coroutineScope {
        while (true) {
            logFlow.collect {
                _state.value = _state.value.parseAll(it)
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
            moveTo(errFile, Config.plotLogsFinished.resolve(errFile.name))
        } else {
            moveTo(logFile, Config.plotLogsFailed.resolve("log${state.value.plotId}.log"))
            moveTo(errFile, Config.plotLogsFailed.resolve("err${state.value.plotId}.log"))
        }
        kill()
    }

    fun moveTo(file: File, destination: File): Boolean {
        if(!file.exists()) return false
        destination.delete()
        file.copyTo(destination)
        return file.delete()
    }
}