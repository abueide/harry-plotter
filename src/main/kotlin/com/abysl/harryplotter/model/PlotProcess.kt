@file:UseSerializers(FileSerializer::class)
package com.abysl.harryplotter.model

import com.abysl.harryplotter.util.serializers.FileSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.periodUntil
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.Transient
import java.io.File
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime

@Serializable
data class PlotProcess(
    val pid: Long,
    val startTime: Instant,
    val logFile: File,
    val errFile: File
    ){

    @Transient
    val process: ProcessHandle? = ProcessHandle.of(pid).orElseGet { null }

    @OptIn(ExperimentalTime::class)
    val timeFlow: Flow<Double> = flow {
        val timeRunning = Clock.System.now() - startTime
        emit(timeRunning.toDouble(TimeUnit.SECONDS))
    }

    val currentState: Flow<JobState> = flow {
        var jobState = JobState()
        if(!logFile.exists() || errFile.readLines().isNotEmpty()){
            emit(JobState(running = false))
        }else {
            logs.collect { line ->
                jobState = jobState.parse(line)
            }
            emit(jobState)
        }
    }

    private val logs: Flow<String> = flow {
        emitAll(logFile.readLines().asFlow())
    }

    fun isRunning(): Boolean {
        return process != null && process.isAlive && logFile.exists()
    }

    fun kill(){
        process?.destroyForcibly()
    }
}