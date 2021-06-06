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

import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.model.TimeEnum
import javafx.application.Platform
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.chart.LineChart
import javafx.scene.chart.XYChart
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.net.URL
import java.util.ResourceBundle

class StatsView : Initializable {
    @FXML
    lateinit var plotsPerXChart: LineChart<String, Int>

    @FXML
    lateinit var timeCombo: ComboBox<TimeEnum>

    @FXML
    lateinit var totalPlots: Label

    @FXML
    lateinit var averagePlotsDay: Label

    @FXML
    lateinit var averagePlotTime: Label

    @FXML
    lateinit var recentLabel: Label

    @FXML
    lateinit var recentPlotsCompleted: Label

    @FXML
    lateinit var recentAveragePlots: Label

    @FXML
    lateinit var recentAveragePlotTime: Label

    lateinit var viewModel: StatsViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        timeCombo.items.setAll(*TimeEnum.values())
        timeCombo.selectionModel.selectFirst()
        plotsPerXChart.legendVisibleProperty().set(false)
    }

    fun initialized(viewModel: StatsViewModel) {
        this.viewModel = viewModel
        timeCombo.valueProperty().bindBidirectional(viewModel.selectedTime)
        viewModel.selectedTime.addListener { _, old, new -> updateTime() }
        viewModel.shownResults.addListener(
            ListChangeListener {
                update()
            }
        )

        totalPlots.textProperty().bind(viewModel.totalPlots.asString())
        averagePlotsDay.textProperty().bind(viewModel.averagePlotsDay.asString())
        averagePlotTime.textProperty().bind(viewModel.averagePlotTime)
        recentPlotsCompleted.textProperty().bind(viewModel.recentTotal.asString())
        recentAveragePlots.textProperty().bind(viewModel.recentAveragePlots.asString())
        recentAveragePlotTime.textProperty().bind(viewModel.recentAveragePlotTime)

        updateTime()
    }

    fun update() {
        CoroutineScope(Dispatchers.IO).launch {
            updateChart()
        }
    }

    suspend fun updateChart() = coroutineScope {
        val series = XYChart.Series<String, Int>()
        viewModel.getPoints().forEach { (xLabel, yValue) ->
            series.data.add(XYChart.Data(xLabel, yValue))
        }
        Platform.runLater {
            plotsPerXChart.data.setAll(series)
        }
    }

    fun onLoadLogs() {
        CoroutineScope(Dispatchers.Default).launch {
            viewModel.loadLogs()
        }
    }

    fun updateTime() {
        val time = viewModel.selectedTime.get()
        plotsPerXChart.title = "Plots Per ${time.titleName}"
        recentLabel.text = "Past ${time.titleName}"
        Prefs.selectedTime = time.name
        update()
    }
}
