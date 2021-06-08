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

package com.abysl.harryplotter.ui.stats

import com.abysl.harryplotter.config.Config
import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.model.jobs.JobResult
import com.abysl.harryplotter.model.jobs.JobState
import com.abysl.harryplotter.model.TimeEnum
import com.abysl.harryplotter.model.TimeEnum.Companion.SECONDS_IN_DAY
import com.abysl.harryplotter.util.formatted
import com.abysl.harryplotter.util.pmap
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

private const val fakeData = false
private const val numFakePoints = 10000

@OptIn(ExperimentalTime::class)
class StatsViewModel(initialResults: List<JobResult> = listOf()) {
    private val updateScope = CoroutineScope(Dispatchers.Default)
    private val processedFiles: MutableStateFlow<Set<String>> = MutableStateFlow(setOf())
    private fun randRange(): Duration = Duration.hours(1)
    private val fakePoints = generateSequence(Clock.System.now()) {
        val randTime = Duration.seconds(Random.nextLong(0, randRange().inWholeSeconds))
        it - randTime
    }

    val shownResults = FXCollections.observableArrayList(initialResults)
    val selectedTime = SimpleObjectProperty(TimeEnum.valueOf(Prefs.selectedTime.uppercase()))

    val totalPlots = SimpleIntegerProperty()
    val averagePlotsDay = SimpleDoubleProperty()
    val averagePlotTime = SimpleStringProperty()

    val recentTotal = SimpleIntegerProperty()
    val recentAveragePlots = SimpleDoubleProperty()
    val recentAveragePlotTime = SimpleStringProperty()

    init {
        selectedTime.addListener { observable, old, new ->
            update()
        }
        shownResults.addListener(
            ListChangeListener {
                update()
            }
        )
        update()
    }

    fun update() {
        updateScope.launch {
            loadLogs()
            withContext(Dispatchers.JavaFx) {
                totalPlots.set(getPlotsDone())
                averagePlotsDay.set(getAveragePlotsDay())
                averagePlotTime.set(readableSeconds(getAveragePlotTime()))
                val range = selectedTime.get().unit
                recentTotal.set(getPlotsDone(range))
                recentAveragePlots.set(getAveragePlotsDay(range))
                recentAveragePlotTime.set(readableSeconds(getAveragePlotTime(range)))
            }
        }
    }

    suspend fun loadLogs() = coroutineScope {
        val results = Config.plotLogsFinished.listFiles()?.asList()
            ?.filter { it.path !in processedFiles.value }
            ?.pmap {
                val job = JobState.parseFile(it)
                processedFiles.value += it.path
                return@pmap job.currentResult
            }?.filter {
                it.timeStarted != null && it.timeCompleted != null && it.totalTime != 0.0
            } ?: listOf()

        withContext(Dispatchers.JavaFx) {
            shownResults.addAll(results)
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

    private fun genPointsMap(completeTimes: List<Instant>): Map<String, Int> {
        val time = Clock.System.now()
        val selectedUnit = selectedTime.get()
        return completeTimes
            // only get points which are within the scope of the zoom range
            .sortedDescending()
            .filter { timeCompleted ->
                val howLongAgo: Duration = time - timeCompleted
                howLongAgo < selectedUnit.zoom
            }
            .foldRight(mapOf<String, Int>()) { timeCompleted, acc ->
                val unit = selectedUnit.getLabel(timeCompleted)
                acc + Pair(unit, 1 + (acc[unit] ?: 0))
            }
    }

    private fun readableSeconds(seconds: Double): String {
        return Duration.seconds(seconds).formatted()
    }

    // Stat Calculations
    private fun getPlotsDone(duration: Duration? = null): Int {
        return getPlotsInRange(duration).size
    }

    // in seconds
    private fun getAveragePlotTime(duration: Duration? = null): Double {
        val results = getPlotsInRange(duration)
        if (results.isEmpty()) return 0.0
        return results.sumOf { it.totalTime } / results.size
    }

    private fun getAveragePlotsDay(duration: Duration? = null): Double {
        val averageTime: Double = getAveragePlotTime(duration)
        if (averageTime == 0.0) return 0.0
        return SECONDS_IN_DAY / getAveragePlotTime(duration)
    }

    private fun getPlotsInRange(duration: Duration? = null): List<JobResult> {
        if (duration == null) return shownResults

        val time = Clock.System.now()
        return shownResults.filter {
            val howLongAgo: Duration = time - (it.timeCompleted ?: return@filter false)
            howLongAgo < duration
        }
    }
}
