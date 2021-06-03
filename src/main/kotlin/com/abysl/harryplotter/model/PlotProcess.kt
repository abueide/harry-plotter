@file:UseSerializers(FileSerializer::class)

package com.abysl.harryplotter.model

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
