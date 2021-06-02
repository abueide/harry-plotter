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

package com.abysl.harryplotter.viewmodel

import com.abysl.harryplotter.config.Config
import com.abysl.harryplotter.model.JobResult
import com.abysl.harryplotter.model.JobState
import com.abysl.harryplotter.model.TimeEnum
import com.abysl.harryplotter.util.pmap
import javafx.application.Platform
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

private const val fakeData = true
private const val numFakePoints = 10000

@OptIn(ExperimentalTime::class)
class StatsViewModel(initialResults: List<JobResult> = listOf()) {
    var counter = 1
    private fun incRange(): Duration = Duration.hours(counter++)
    private fun randRange():Duration = Duration.days(1)

    val shownResults = FXCollections.observableArrayList(initialResults)

    val totalPlots = SimpleIntegerProperty(0)
    val averagePlotsDay = SimpleDoubleProperty(0.0)
    val averagePlotTime = SimpleStringProperty("")
    val selectedTime = SimpleObjectProperty(TimeEnum.WEEKLY)
    val recentLabel = "${selectedTime.get()}"

    init {
        selectedTime.addListener { observable, old, new ->
            println(new)
        }
    }

    fun getPoints(): Map<String, Int> {
        val points: List<Instant> = shownResults
            .mapNotNull { it.timeCompleted }
        return if (!fakeData) {
            genPointsMap(points)
        } else {
            genPointsMap(fakePoints.take(numFakePoints).toList())
        }
    }

    val fakePoints = generateSequence(Clock.System.now()) {
        val randTime = Duration.seconds(Random.nextLong(0, randRange().inWholeSeconds))
        it - randTime
    }

    val totalPlotsFlow = flow {
        emit(shownResults.size)
    }


    fun loadLogs() {
        CoroutineScope(Dispatchers.Default).launch {
            val results = Config.plotLogsFinished.listFiles()?.asList()?.pmap {
                JobState.parseFile(it).currentResult
            } ?: listOf()
            Platform.runLater {
                shownResults.clear()
                shownResults.addAll(results)
            }
        }
    }

    private fun genPointsMap(completeTimes: List<Instant>): Map<String, Int> {
        val time = Clock.System.now()
        val selectedUnit = selectedTime.get()
        return completeTimes
            // only get points which are within the scope of the zoom range
            .sortedDescending()
            .filter {
                    timeCompleted ->
                val howLongAgo: Duration = time - timeCompleted
                howLongAgo < selectedUnit.zoom
            }
            .foldRight(mapOf<String, Int>()) { timeCompleted, acc  ->
                val unit = selectedUnit.getLabel(timeCompleted)
                acc + Pair(unit, 1 + (acc[unit] ?: 0))
            }
    }
}