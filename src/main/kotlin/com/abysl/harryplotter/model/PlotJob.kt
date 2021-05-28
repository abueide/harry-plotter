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

import com.abysl.harryplotter.util.serializers.FileSerializer
import com.abysl.harryplotter.chia.ChiaCli
import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.model.records.JobDescription
import com.abysl.harryplotter.model.records.JobStats
import com.abysl.harryplotter.util.IOUtil
import com.abysl.harryplotter.util.serializers.MutableStateFlowSerializer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import java.io.File
import java.util.Locale

@Serializable
class PlotJob(
    var description: JobDescription,
    val statsFlow: MutableStateFlow<JobStats> = MutableStateFlow(JobStats()),
    var plotProcess: PlotProcess? = null
) {
    @Transient
    var tempDone = 0

    @Transient
    var manageSelf = false

    var stats
        get() = statsFlow.value
        set(value) {
            statsFlow.value = value
        }

    fun start(manageSelf: Boolean = false) {
        if(plotProcess == null){
            this.manageSelf = manageSelf
            val chia = ChiaCli(File(Prefs.exePath), File(Prefs.configPath))
            plotProcess = chia.createPlot(description)
        }else {
            println("Trying to start job while job is running, ignoring request.")
        }
    }

    // block boolean used so that we can finish deleting temp files before the program exits. Otherwise, we don't want
    // to block the main thread while deleting files.
    fun stop(block: Boolean = false) {
        plotProcess?.let {
            it.kill()
            val temp = CoroutineScope(Dispatchers.IO).async {
                it.currentState.collect { state ->
                    deleteTempFiles(state.plotId, block)
                }
            }
            if(block){
                runBlocking {
                    temp.await()
                }
            }
        }
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

    private fun whenDone(time: Double) {
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

//    val percentageFlow: Flow<Double> = flow {
//        proces
//        timeFlow.collectLatest { emit(it / stats.averagePlotTime * 100) }
//    }

    override fun toString(): String {
        val roundedPercentage =
            if (state.percentage.isNaN() || state.percentage.isInfinite()) "?"
            else String.format(Locale.US, "%.2f", state.percentage * 100)
        return if (state.running) "$description - $roundedPercentage%" else description.toString()
    }

    fun getPercentage(): Double{
        return state.time
    }

    companion object {
        private const val REFRESH_DELAY = 1000L
    }
}
