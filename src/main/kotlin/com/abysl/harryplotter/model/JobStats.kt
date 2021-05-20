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

import com.abysl.harryplotter.util.serializers.MutableStateFlowSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class JobStats(
    var plotsDone: Int = 0,
    val results: MutableList<JobResult> = mutableListOf()
) {
    val plotsDoneFlow: Flow<Int> = flow { emit(plotsDone) }
    val lastPlotTime: Flow<Double> = flow { emit(results.lastOrNull()?.totalTime ?: 0.0) }
    val averagePlotTime: Flow<Double> = flow {
        if (results.isEmpty())
            emit(0.0)
        else
            emit(results.sumOf { it.totalTime } / results.size)
    }
    val estimatedPlotsDay: Flow<Double> = flow {
        when (val time = lastPlotTime.last()) {
            0.0 -> emit(0.0)
            else -> emit(SECONDS_IN_DAY / time)
        }
    }
    val averagePlotsDay: Flow<Double> = flow { SECONDS_IN_DAY / averagePlotTime.last() }

    companion object {
        private const val SECONDS_IN_DAY: Int = 86400
    }
}

