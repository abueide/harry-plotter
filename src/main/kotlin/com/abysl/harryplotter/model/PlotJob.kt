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

@file:UseSerializers(MutableStateFlowSerializer::class, FileSerializer::class)

package com.abysl.harryplotter.model

import com.abysl.harryplotter.chia.ChiaCli
import com.abysl.harryplotter.model.records.JobDescription
import com.abysl.harryplotter.model.records.JobStats
import com.abysl.harryplotter.util.IOUtil
import com.abysl.harryplotter.util.invoke
import com.abysl.harryplotter.util.serializers.FileSerializer
import com.abysl.harryplotter.util.serializers.MutableStateFlowSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import java.util.Locale

@Serializable
class PlotJob(
    var description: JobDescription,
    val statsFlow: MutableStateFlow<JobStats> = MutableStateFlow(JobStats()),
    var process: MutableStateFlow<PlotProcess?> = MutableStateFlow(null)
) {

    @Transient
    var tempDone = 0

    @Transient
    var manageSelf = false

    @Transient
    var updateScope = CoroutineScope(Dispatchers.IO)

    @Transient
    var doneCallback: (() -> Unit)? = null

    var stats
        get() = statsFlow.value
        set(value) {
            statsFlow.value = value
        }

    val state get() = process()?.state?.value ?: JobState()

    fun initialized(doneCallback: (() -> Unit)? = null) {
        this.doneCallback = doneCallback
        process()?.let {
            it.initialized(::whenDone)
        }
        updateScope.launch {
            process()?.update(REFRESH_DELAY)
        }
    }

    fun start(manageSelf: Boolean = false) {
        if (process()?.isRunning() != true) {
            process()?.dispose()
            this.manageSelf = manageSelf
            val chia = ChiaCli()
            val proc = chia.createPlot(description, this::whenDone)
            process.value = proc
            updateScope.launch {
                proc?.update(REFRESH_DELAY)
            }
        } else {
            println("Trying to start job while job is running, ignoring request.")
        }
    }

    // block boolean used so that we can finish deleting temp files before the program exits. Otherwise, we don't want
    // to block the main thread while deleting files.
    fun stop(block: Boolean = false) {
        manageSelf = false
        process()?.let { proc ->
            val state = proc.state()
            proc.dispose()
            val temp = CoroutineScope(Dispatchers.IO).async {
                deleteTempFiles(state.plotId, block)
            }
            if (block) {
                runBlocking {
                    temp.await()
                }
            }
        }
        process.value = null
        updateScope.cancel()
        updateScope = CoroutineScope(Dispatchers.IO)
    }

    private fun deleteTempFiles(plotId: String, block: Boolean) {
        if (plotId.isNotBlank()) {
            val files = description.tempDir.listFiles()
                ?.filter { it.toString().contains(plotId) && it.extension == "tmp" }
                ?.map(IOUtil::deleteFile)
            if (block) {
                runBlocking {
                    files?.forEach { it.await() }
                }
            }
        }
    }

    private fun whenDone() {
        val proc = process() ?: return
        val state = if (checkCompleted()) proc.state.value.setComplete() else proc.state.value
        proc.state.value = state.copy()
        if (state.completed) {
            stats = stats.plotDone(state.currentResult)
            tempDone++
            if (manageSelf && (tempDone < description.plotsToFinish || description.plotsToFinish == 0)) {
                stop()
                start(manageSelf = manageSelf)
            } else {
                stop()
            }
        } else {
            stop()
        }
        doneCallback?.invoke()
    }

    fun isReady(): Boolean {
        return !isRunning() && (description.plotsToFinish == 0 || tempDone < description.plotsToFinish)
    }

    fun percentDone(): Double {
        val proc = process() ?: return 0.0
        return (proc.timeRunning() / stats.averagePlotTime) * 100
    }

    fun isRunning(): Boolean {
        val proc = process() ?: return false
        return proc.isRunning()
    }

    fun checkCompleted(): Boolean {
        if (state.plotId.isBlank()) return false
        val files = description.destDir.listFiles()
        return files?.any {
            it.name.contains(state.plotId)
        } == true
    }

    override fun toString(): String {
        if (process() == null) return description.toString()
        val percentage = percentDone()
        val roundedPercentage =
            if (percentage.isNaN() || percentage.isInfinite()) "?"
            else String.format(Locale.US, "%.2f", percentage)
        return "$description - $roundedPercentage%"
    }

    companion object {
        private const val REFRESH_DELAY = 100L
    }
}
