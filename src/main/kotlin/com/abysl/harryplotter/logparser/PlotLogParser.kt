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

package com.abysl.harryplotter.logparser

import com.abysl.harryplotter.model.JobResult
import com.abysl.harryplotter.model.JobState

object PlotLogParser {
    fun parseLine(jobState: JobState = JobState(), line: String, appendLog: Boolean = true): JobState {
        return try {
            jobState.copy(
                plotId = parsePlotId(line) ?: jobState.plotId,
                phase = parsePhase(line) ?: jobState.phase,
                subphase = parseTable(line) ?: jobState.subphase,
                currentResult = parseResult(line) ?: jobState.currentResult,
                logs = if (appendLog) jobState.logs + line else jobState.logs
            )
        } catch (e: Exception) {
            println("Parser failed! $e")
            jobState
        }
    }

    fun parsePlotId(line: String): String? {
        if (!line.contains("ID: ")) return null
        return line.split("ID: ").lastOrNull()
    }

    fun parsePhase(line: String): Int? {
        if (!line.contains("Starting phase")) return null
        return line
            .split("Starting phase ").lastOrNull()
            ?.split("/")?.firstOrNull()
            ?.toInt()
    }

    fun parseTable(line: String): String? {
        return when {
            line.contains("tables") -> {
                line.split("tables ").lastOrNull()
            }
            line.contains("table") -> {
                line.split("table ").lastOrNull()
            }
            else -> null
        }
    }

    fun parseResult(line: String): JobResult? {
        if (line.contains("Time for phase") || line.contains("Total time") || line.contains("Copy time")) {
            val seconds: Double = line
                .split("= ").lastOrNull()
                ?.split(" seconds")?.firstOrNull()
                ?.toDouble() ?: return null

            when {
                line.contains("Time for phase") -> {
                    val phase: Int = line
                        .split("phase ").lastOrNull()
                        ?.split(" =")?.firstOrNull()
                        ?.toInt() ?: return null
                    return when (phase) {
                        1 -> JobResult(phaseOneTime = seconds)
                        2 -> JobResult(phaseTwoTime = seconds)
                        3 -> JobResult(phaseThreeTime = seconds)
                        4 -> JobResult(phaseFourTime = seconds)
                        else -> null
                    }
                }
                line.contains("Total time") -> {
                    return JobResult(totalTime = seconds)
                }
                line.contains("Copy time") -> {
                    return JobResult(copyTime = seconds)
                }
                else -> return null
            }
        } else
            return null
    }
}
