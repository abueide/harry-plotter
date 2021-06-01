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
import kotlinx.datetime.Instant
import java.lang.NumberFormatException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object PlotLogParser {
    private const val START_TIME_KEY = "Starting phase 1/4: Forward Propagation into tmp files... "


    fun parseLine(jobState: JobState = JobState(), line: String): JobState {
        val result = parseResult(line) ?: jobState.currentResult
        return try {
            jobState.copy(
                plotId = parsePlotId(line) ?: jobState.plotId,
                phase = parsePhaseStart(line) ?: jobState.phase,
                subphase = parseTable(line) ?: jobState.subphase,
                currentResult = result + jobState.currentResult,
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

    fun parsePhaseStart(line: String): Int? {
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
            when {
                line.contains("Time for phase") -> {
                    return when (parsePhaseTime(line)) {
                        1 -> JobResult(phaseOneTime = parseSeconds(line))
                        2 -> JobResult(phaseTwoTime = parseSeconds(line))
                        3 -> JobResult(phaseThreeTime = parseSeconds(line))
                        4 -> JobResult(phaseFourTime = parseSeconds(line))
                        else -> null
                    }
                }
                line.contains("Copy time") -> {
                    return JobResult(copyTime = parseSeconds(line))
                }
                line.contains("Total time") -> {
                    return JobResult(totalTime = parseSeconds(line), timeCompleted = parseEnd(line))
                }
                line.contains(START_TIME_KEY) -> return JobResult(timeStarted = parseStart(line))
                else -> return null
            }
        }

    fun parseSeconds(line: String): Double {
        return line
            .split("= ").lastOrNull()
            ?.split(" seconds")?.firstOrNull()
            ?.toDouble() ?: 0.0
    }

    fun parsePhaseTime(line: String): Int {
        return line
            .split("phase ").lastOrNull()
            ?.split(" =")?.firstOrNull()
            ?.toInt() ?: 0
    }

    fun parseStart(line: String): Instant? {
        val sdf = SimpleDateFormat("EE MMM dd HH:mm:ss yyyy", Locale.ENGLISH)
            .also { it.timeZone = TimeZone.getDefault() }
        val time = line
            .replace("\"", "")
            .split(START_TIME_KEY)
            .lastOrNull()
            ?.ifBlank { null } ?: return null
        return try {
            val result = Instant.fromEpochSeconds(sdf.parse(time).toInstant().epochSecond)
            result
        }catch (exception: NumberFormatException){
            exception.printStackTrace()
            null
        }
    }

    private val END_TIME_KEY = "Total time ="
    fun parseEnd(line: String): Instant? {
        val sdf = SimpleDateFormat("EE MMM dd HH:mm:ss yyyy", Locale.ENGLISH)
            .also { it.timeZone = TimeZone.getDefault() }
        val time = line
            .replace("\"", "")
            .split(END_TIME_KEY).lastOrNull()
            ?.split(") ")?.lastOrNull()
            ?.ifBlank { null } ?: return null
        return try {
            val result = Instant.fromEpochSeconds(sdf.parse(time).toInstant().epochSecond)
            result
        }catch (exception: NumberFormatException){
            exception.printStackTrace()
            null
        }
    }

}
