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

package com.abysl.harryplotter.model

import com.abysl.harryplotter.util.invoke
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable


data class JobState(
    val proc: MutableStateFlow<Process?> = MutableStateFlow(null),
    val running: MutableStateFlow<Boolean> = MutableStateFlow(false),
    val phase: MutableStateFlow<Int> = MutableStateFlow(1),
    val subphase: MutableStateFlow<String> = MutableStateFlow(""),
    val plotId: MutableStateFlow<String> = MutableStateFlow(""),
    val percentage: MutableStateFlow<Double> = MutableStateFlow(0.0),
    val secondsRunning: MutableStateFlow<Long> = MutableStateFlow(0),
    val currentResult: MutableStateFlow<JobResult> = MutableStateFlow(JobResult()),
    val results: MutableStateFlow<List<JobResult>> = MutableStateFlow(mutableListOf()),
    val logs: MutableStateFlow<List<String>> = MutableStateFlow(mutableListOf()),
) {
    fun reset() {
        proc(null)
        running(false)
        phase(1)
        subphase("")
        plotId("")
        percentage(0.0)
        secondsRunning(0)
        currentResult(JobResult())
        results(listOf())
        logs(listOf())
    }
}
