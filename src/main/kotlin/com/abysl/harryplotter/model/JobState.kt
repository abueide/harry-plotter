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


data class JobState(
    var proc: Process? = null,
    var running: Boolean = false,
    var phase: Int = 1,
    var subphase: String = "",
    var plotId: String = "",
    var percentage: Double = 0.0,
    var secondsRunning: Long = 0,
    var currentResult: JobResult = JobResult(),
    val results: MutableList<JobResult> = mutableListOf(),
    var logs: MutableList<String> = mutableListOf(),
)
