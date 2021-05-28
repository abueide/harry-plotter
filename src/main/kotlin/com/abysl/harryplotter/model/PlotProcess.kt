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
    val errFile: File,
    @Transient
    val onComplete: () -> Unit = {}
) {

    @Transient
    private var stateCounter = 0

    @Transient
    private val _state: MutableStateFlow<JobState> = MutableStateFlow(JobState())

    @Transient
    private var _logs: MutableList<String> = mutableListOf()

    @Transient
    private var _cache = false

    @Transient
    val state: StateFlow<JobState> = _state.asStateFlow()

    @Transient
    private val process get() = ProcessHandle.of(pid).orElseGet { null }

    @Transient
    val logs: List<String> get() = _logs

    @Transient
    var cache: Boolean
        get() = _cache
        set(value) {
            _cache = value
            if (_cache) _logs.clear()
        }

    suspend fun update(delay: Long) = coroutineScope {
        while (true) {
            var lineNum = 0
            logFile.forEachLine {
                if (cache && lineNum >= _logs.size) {
                    _logs.add(it)
                }
                if (lineNum >= stateCounter) {
                    _state.value = _state().parse(it)
                    if (_state().completed) onComplete()
                }
                lineNum++
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

    fun dispose(){
        val updatedState = state.value
        if(updatedState.completed){
            logFile.copyTo(Config.plotLogsFailed.resolve(logFile.name))
            logFile.delete()
        }else {
            logFile.copyTo(Config.plotLogsFailed.resolve(logFile.name))
            errFile.copyTo(Config.plotLogsFailed.resolve(errFile.name))
            logFile.delete()
            errFile.delete()
        }
        _logs.clear()
        kill()
    }
}