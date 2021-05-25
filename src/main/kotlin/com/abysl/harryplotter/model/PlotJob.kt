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

@file:UseSerializers(MutableStateFlowSerializer::class)

package com.abysl.harryplotter.model

import com.abysl.harryplotter.model.records.JobDescription
import com.abysl.harryplotter.model.records.JobStats
import com.abysl.harryplotter.util.IOUtil.deleteFile
import com.abysl.harryplotter.util.serializers.MutableStateFlowSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.Locale

@Serializable
class PlotJob(
    var description: JobDescription,
    val statsFlow: MutableStateFlow<JobStats> = MutableStateFlow(JobStats())
) {

    @Transient
    val stateFlow: MutableStateFlow<JobState> = MutableStateFlow(JobState())

    @Transient
    var timerScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    var stats
        get() = statsFlow.value
        set(value) {
            statsFlow.value = value
        }
    var state
        get() = stateFlow.value
        set(value) {
            stateFlow.value = value
        }

    var tempDone = 0
    var manageSelf = false

    fun start(manageSelf: Boolean = false) {
        this.manageSelf = manageSelf

        if (state.running || state.proc?.isAlive == true) {
            println("Trying to start new process while old one is still running, ignoring start job.")
        } else {
            val proc = description.launch(
                ioDelay = 10,
                onOutput = ::parseLine,
                onCompleted = ::whenDone,
            )
            state = state.copy(running = true, proc = proc)
        }

        timerScope.launch {
            while (true) {
                val proc = state.proc
                if (proc != null) {
                    val time = proc.info()
                        ?.startInstant()
                        ?.map { Duration.between(it, Instant.now()) }
                        ?.orElse(null)
                        ?.seconds?.toDouble() ?: 0.0
                    val percentage: Double = time / stats.averagePlotTime
                    state = state.copy(percentage = percentage)
                }
                delay(REFRESH_DELAY)
            }
        }
    }

    // block boolean used so that we can finish deleting temp files before the program exits. Otherwise, we don't want
    // to block the main thread while deleting files.
    fun stop(block: Boolean = false) {
        timerScope.cancel()
        timerScope = CoroutineScope(Dispatchers.IO)
        // Store in immutable variable so it doesn't try to delete files after state is wiped out
        state.proc?.destroyForcibly()
        deleteTempFiles(state.plotId, block)
        state = state.reset()
    }

    private fun deleteTempFiles(plotId: String, block: Boolean) {
        if (plotId.isNotBlank()) {
            val files = description.tempDir.listFiles()
                ?.filter { it.toString().contains(plotId) && it.extension == "tmp" }
                ?.map { deleteFile(it) }
            if (block) {
                runBlocking {
                    files?.forEach { it.await() }
                }
            }
        }
    }

    private fun whenDone(time: Double) {
        if (state.phase == 4 && state.currentResult.totalTime == 0.0) {
            state = state.copy(currentResult = JobResult(totalTime = time))
        }
        if (state.currentResult.totalTime > 0.0) {
            stats = stats.plotDone(state.currentResult)
            tempDone++
        }
        if (manageSelf && state.running && (tempDone < description.plotsToFinish || description.plotsToFinish == 0)) {
            stop()
            start(manageSelf = true)
        } else {
            stop()
        }
    }

    fun parseLine(line: String) {
        state = state.parse(line)
    }

    fun isReady(): Boolean {
        return !state.running && (description.plotsToFinish == 0 || tempDone < description.plotsToFinish)
    }

    override fun toString(): String {
        val roundedPercentage =
            if (state.percentage.isNaN() || state.percentage.isInfinite()) "?"
            else String.format(Locale.US, "%.2f", state.percentage * 100)
        return if (state.running) "$description - $roundedPercentage%" else description.toString()
    }

    companion object {
        private const val REFRESH_DELAY = 1000L
    }
}
