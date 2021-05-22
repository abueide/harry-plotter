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

import com.abysl.harryplotter.chia.ChiaCli
import com.abysl.harryplotter.model.records.JobDescription
import com.abysl.harryplotter.model.records.JobStats
import com.abysl.harryplotter.util.serializers.MutableStateFlowSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import java.io.File
import java.time.Duration
import java.time.Instant

@Serializable
data class PlotJob(
    var description: JobDescription,
    val statsFlow: MutableStateFlow<JobStats> = MutableStateFlow(JobStats())
) {

    @Transient
    val stateFlow: MutableStateFlow<JobState> = MutableStateFlow(JobState())

    var stats
        get() = statsFlow.value
        set(jobStats) {
            statsFlow.value = jobStats
        }
    var state
        get() = stateFlow.value
        set(jobState) {
            stateFlow.value = jobState
        }

    fun start() {
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
    }

    // block boolean used so that we can finish deleting temp files before the program exits. Otherwise, we don't want
    // to block the main thread while deleting files.
    fun stop(block: Boolean = false) {
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

    private fun deleteFile(file: File, delayTime: Long = 100, maxTries: Int = 100) =
        CoroutineScope(Dispatchers.IO).async {
            var timeout = 0
            while (file.exists() && !file.delete() && timeout++ < maxTries) {
                if (timeout > 1) {
                    println("Couldn't delete file, trying again in $delayTime ms. ${file.name}")
                }
                delay(delayTime)
            }
            if (timeout < maxTries) {
                println("Deleted: " + file.name)
                return@async true
            } else {
                return@async false
            }
        }

    private fun whenDone(time: Double) {
        if(state.phase == 4 && state.currentResult.totalTime == 0.0) {
            state = state.copy(currentResult = JobResult(totalTime = time))
        }
        if (state.currentResult.totalTime > 0.0) {
            stats = stats.plotDone(state.currentResult)
        }
        if (state.running
            && state.currentResult.totalTime > 0.0
            && (stats.plotsDone < description.plotsToFinish || description.plotsToFinish == 0)) {

            stop()
            start()
        } else {
            stop()
        }
    }

    fun parseLine(line: String) {
        state = state.parse(line)
    }

    override fun toString(): String {
        return if (state.running) "$description - ${state.percentage}%" else description.toString()
    }
}
