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

package com.abysl.harryplotter.model.records

import com.abysl.harryplotter.model.JobResult
import com.abysl.harryplotter.util.serializers.MutableStateFlowSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class JobStats(
    val plotsDone: Int = 0,
    val results: List<JobResult> = mutableListOf()
) {
    val lastPlotTime: Double by lazy { results.lastOrNull()?.totalTime ?: 0.0 }
    val averagePlotTime: Double by lazy {
        when {
            results.isEmpty() -> 0.0
            else -> results.sumOf { it.totalTime } / results.size
        }
    }
    val estimatedPlotsDay: Double by lazy {
        when (lastPlotTime) {
            0.0 -> 0.0
            else -> SECONDS_IN_DAY / lastPlotTime
        }
    }
    val averagePlotsDay: Double by lazy { if (averagePlotTime != 0.0) SECONDS_IN_DAY / averagePlotTime else 0.0 }

    fun plotDone(result: JobResult): JobStats {
        return copy(plotsDone = plotsDone + 1, results = results + result)
    }

    companion object {
        private const val SECONDS_IN_DAY: Int = 86400
    }
}
