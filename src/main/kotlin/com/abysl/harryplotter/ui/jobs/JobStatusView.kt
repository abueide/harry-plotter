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

package com.abysl.harryplotter.ui.jobs

import com.abysl.harryplotter.model.drives.CacheDrive
import com.abysl.harryplotter.model.drives.Drive
import com.abysl.harryplotter.model.jobs.PlotJob
import com.abysl.harryplotter.ui.all.SimpleDialogs.showAlert
import com.abysl.harryplotter.ui.all.SimpleDialogs.showConfirmation
import com.abysl.harryplotter.util.formatted
import com.abysl.harryplotter.util.invoke
import com.abysl.harryplotter.util.unlines
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class JobStatusView {

    @FXML
    private lateinit var plotId: Label

    @FXML
    private lateinit var currentStatus: Label

    @FXML
    private lateinit var totalPlotsCreated: Label

    @FXML
    private lateinit var lastPlotTime: Label

    @FXML
    private lateinit var averagePlotTime: Label

    @FXML
    private lateinit var estimatedPlotsDay: Label

    @FXML
    private lateinit var logsWindow: TextArea

    @FXML
    private lateinit var p1Time: Label

    @FXML
    private lateinit var p2Time: Label

    @FXML
    private lateinit var p3Time: Label

    @FXML
    private lateinit var p4Time: Label

    @FXML
    private lateinit var copyTime: Label

    lateinit var viewModel: JobStatusViewModel

    lateinit var drives: MutableStateFlow<List<Drive>>

    var loadLogScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    var jobBinding: CoroutineScope = CoroutineScope(Dispatchers.IO)
    var fxBinding: CoroutineScope = CoroutineScope(Dispatchers.JavaFx)

    fun initialized(jobStatusViewModel: JobStatusViewModel, drives: MutableStateFlow<List<Drive>>) {
        this.viewModel = jobStatusViewModel
        this.drives = drives
        viewModel.shownJob.mapLatest(::bind).launchIn(CoroutineScope(Dispatchers.IO))
    }

    fun bind(plotJob: PlotJob?) {
        unbind()
        plotJob ?: return
        plotJob.statsFlow.onEach {
            fxBinding.launch {
                totalPlotsCreated.text = it.plotsDone.toString()
                lastPlotTime.text = Duration.seconds(it.lastPlotTime.toLong()).formatted()
                averagePlotTime.text = Duration.seconds(it.averagePlotTime.toLong()).formatted()
                estimatedPlotsDay.text = it.estimatedPlotsDay.toString()
                val result = it.results.lastOrNull()
                if (result != null) {
                    p1Time.text = Duration.seconds(result.phaseOneTime.toLong()).formatted()
                    p2Time.text = Duration.seconds(result.phaseTwoTime.toLong()).formatted()
                    p3Time.text = Duration.seconds(result.phaseThreeTime.toLong()).formatted()
                    p4Time.text = Duration.seconds(result.phaseFourTime.toLong()).formatted()
                    copyTime.text = Duration.seconds(result.copyTime.toLong()).formatted()
                }
            }
        }.launchIn(jobBinding)
        plotJob.process.onEach { process ->
            process?.state?.onEach {
                fxBinding.launch {
                    currentStatus.text = it.status
                    plotId.text = it.plotId
                }
            }?.launchIn(jobBinding)
            process?.newLogs?.onEach {
                fxBinding.launch { logsWindow.appendText(it.unlines()) }
            }?.launchIn(jobBinding)
            loadLogScope.cancel()
            loadLogScope = process?.readLogs { logs -> fxBinding.launch { logsWindow.appendText(logs) } }
                ?: CoroutineScope(Dispatchers.IO)
        }.launchIn(jobBinding)
    }

    fun unbind() {
        jobBinding.cancel()
        fxBinding.cancel()
        jobBinding = CoroutineScope(Dispatchers.IO)
        fxBinding = CoroutineScope(Dispatchers.JavaFx)
        fxBinding.launch {
            currentStatus.text = ""
            plotId.text = ""
            lastPlotTime.text = ""
            totalPlotsCreated.text = ""
            averagePlotTime.text = ""
            estimatedPlotsDay.text = ""
            p1Time.text = ""
            p2Time.text = ""
            p3Time.text = ""
            p4Time.text = ""
            copyTime.text = ""
            logsWindow.clear()
        }
    }

    fun onStart() {
        when (viewModel.shownJob()) {
            null -> {
                showAlert("No job selected!", "You must save & select your plot job before you run it.")
            }
            else -> {
                val cacheDrive = drives.value.filterIsInstance<CacheDrive>().randomOrNull()
                viewModel.shownJob()?.also { it.tempDone = 0 }?.start(true, cacheDrive?.drivePath)
            }
        }
    }

    fun onStop() {
        val job: PlotJob = viewModel.shownJob() ?: return
        if (job.isRunning() && showConfirmation("Stop Process", "Are you sure you want to stop $job?")) {
            job.stop()
        }
        job.manageSelf = !job.manageSelf
    }
}
