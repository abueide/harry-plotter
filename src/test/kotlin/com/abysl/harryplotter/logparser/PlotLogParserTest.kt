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

import com.abysl.harryplotter.model.jobs.JobResult
import com.abysl.harryplotter.model.jobs.JobState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PlotLogParserTest {
    val phaseTest = "Starting phase 2/4: Forward Propagation into tmp files... Thu May 20 11:12:05 2021"
    val subphaseTest = "Computing table 4"
    val idTest = "ID: 47861611e2574d6ea75573afe1222784341a6afb1a70ed22e6d45df9dc6a79c9"
    val time = 1204.32
    val resultsTotalTest = listOf(
        "Total time = $time seconds",
        "Total time = $time",
    )
    val resultsPhaseTime = listOf(
        "Time for phase 2 = $time seconds",
        "Time for phase 2 = $time"
    )

//    @Test
//    fun parseLine() {
//    }

    @Test
    fun parsePlotId() {
        val test = idTest
        val expected = JobState(plotId = "47861611e2574d6ea75573afe1222784341a6afb1a70ed22e6d45df9dc6a79c9")
        val actual = PlotLogParser.parseLine(line = test)
        assertEquals(expected, actual)
    }

    @Test
    fun parsePhase() {
        val test = phaseTest
        val expected = JobState(phase = 2)
        val actual = PlotLogParser.parseLine(line = test)
        assertEquals(expected, actual)
    }

    @Test
    fun parseTable() {
        val test = subphaseTest
        val expected = JobState(subphase = "4")
        val actual = PlotLogParser.parseLine(line = test)
        assertEquals(expected, actual)
    }

    @Test
    fun parseResult() {
        val expectedTotal = JobState(currentResult = JobResult(totalTime = time))
        resultsTotalTest.forEach {
            val actual = PlotLogParser.parseLine(line = it)
            assertEquals(expectedTotal, actual)
        }
        val expectedPhase = JobState(currentResult = JobResult(phaseTwoTime = time))
        resultsPhaseTime.forEach {
            val actual = PlotLogParser.parseLine(line = it)
            assertEquals(expectedPhase, actual)
        }
    }
}
