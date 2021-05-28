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

import com.abysl.harryplotter.logparser.PlotLogParser
import kotlinx.serialization.Serializable

@Serializable
data class JobState(
    val running: Boolean = false,
    val completed: Boolean = false,
    val phase: Int = 1,
    val subphase: String = "",
    val plotId: String = "",
    val currentResult: JobResult = JobResult(),
) {


    val status by lazy {
        if (running) {
            "$RUNNING: Phase $phase/4"
        } else {
            STOPPED
        }
    }

    fun parse(line: String): JobState {
        return PlotLogParser.parseLine(this, line)
    }

    companion object {
        private const val RUNNING = "Running"
        private const val STOPPED = "Stopped"
    }
}
