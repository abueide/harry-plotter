@file:UseSerializers(FileSerializer::class)

package com.abysl.harryplotter.model

import com.abysl.harryplotter.config.Config
import com.abysl.harryplotter.util.serializers.FileSerializer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime

@Serializable
data class PlotProcess(
    val pid: Long,
    val startTime: Instant,
    val logFile: File,
    val errFile: File,
    val onComplete: () -> Unit
) {

    @Transient
    private var stateCounter = 0

    @Transient
    private var _state: JobState = JobState()

    @Transient
    private var _logs: MutableList<String> = mutableListOf()

    @Transient
    private var _cache = false

    val process get() = ProcessHandle.of(pid).orElseGet { null }

    @Transient
    var cache: Boolean
        get() = _cache
        set(value) {
            _cache = value
            if (_cache) _logs.clear()
        }

    fun state(): JobState {
        logs()
        return _state.copy()
    }

    fun logs(): List<String> {
        var lineNum = 0
        logFile.forEachLine {
            if (cache && lineNum >= _logs.size) {
                _logs.add(it)
            }
            if (lineNum >= stateCounter) {
                _state = _state.parse(it)
                if(_state.completed) onComplete()
            }
            lineNum++
        }
        return _logs
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
        val updatedState = state()
        if(updatedState.completed){

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