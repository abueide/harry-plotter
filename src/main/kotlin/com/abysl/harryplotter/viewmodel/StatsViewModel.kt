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
import kotlin.time.ExperimentalTime

class StatsViewModel(initialResults: List<JobResult> = listOf()) {
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

    @OptIn(ExperimentalTime::class)
    val pointsFlow = flow {
        val time = Clock.System.now()
        val selectedUnit = selectedTime.get()
        val points = shownResults
            // only get points which are within the scope of the zoom range
            .mapNotNull { it.timeCompleted }
            .filter { timeCompleted ->  (time - timeCompleted) <= selectedUnit.zoom }
            .fold(mapOf<String, Int>()) { acc, timeCompleted ->
                val unit = selectedUnit.getLabel(timeCompleted)
                acc + Pair(unit, 1 + (acc[unit] ?: 0))
            }
        emit(points)
    }

    val totalPlotsFlow = flow {
        emit(shownResults.size)
    }

    fun loadLogs(){
        CoroutineScope(Dispatchers.Default).launch {
            val results = Config.plotLogsFinished.listFiles()?.asList()?.pmap{
                JobState.parseFile(it).currentResult
            } ?: listOf()
            Platform.runLater {
                shownResults.clear()
                shownResults.addAll(results)
            }
        }
    }
}