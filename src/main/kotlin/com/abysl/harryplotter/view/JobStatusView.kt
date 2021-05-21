package com.abysl.harryplotter.view

import com.abysl.harryplotter.model.PlotJob
import com.abysl.harryplotter.util.invoke
import com.abysl.harryplotter.util.unlines
import com.abysl.harryplotter.viewmodel.JobStatusViewModel
import com.abysl.harryplotter.windows.SimpleDialogs.showAlert
import com.abysl.harryplotter.windows.SimpleDialogs.showConfirmation
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.runBlocking

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
    private lateinit var averagePlotsDay: Label

    @FXML
    private lateinit var logsWindow: TextArea

    lateinit var viewModel: JobStatusViewModel

    var jobBinding: CoroutineScope = CoroutineScope(Dispatchers.IO)

    fun initialized(jobStatusViewModel: JobStatusViewModel) {
        this.viewModel = jobStatusViewModel
        viewModel.shownJob.onEach(::bind).launchIn(CoroutineScope(Dispatchers.IO))
        viewModel.shownLogs.onEach {
            Platform.runLater {
                if (viewModel.shouldAppend()) {
                    logsWindow.appendText(viewModel.shownLogs().last() + "\n")
                } else {
                    logsWindow.text = viewModel.shownLogs().unlines()
                }
            }
        }.launchIn(CoroutineScope(Dispatchers.IO))
    }

    fun bind(plotJob: PlotJob?) {
        unbind()
        plotJob ?: return
        plotJob.statsFlow.onEach {
            Platform.runLater {
                totalPlotsCreated.text = it.plotsDone.toString()
                lastPlotTime.text = it.lastPlotTime.toString()
                averagePlotTime.text = it.averagePlotTime.toString()
                estimatedPlotsDay.text = it.estimatedPlotsDay.toString()
                averagePlotsDay.text = it.averagePlotsDay.toString()
            }
        }.launchIn(jobBinding)
        plotJob.stateFlow.onEach {
            Platform.runLater {
                currentStatus.text = it.status
                plotId.text = it.plotId
            }
        }.launchIn(jobBinding)
    }

    fun unbind() {
        runBlocking {
            CoroutineScope(Dispatchers.JavaFx).async {
                jobBinding.cancel()
                jobBinding = CoroutineScope(Dispatchers.IO)
                currentStatus.text = ""
                plotId.text = ""
                lastPlotTime.text = ""
                totalPlotsCreated.text = ""
                averagePlotTime.text = ""
                estimatedPlotsDay.text = ""
                averagePlotsDay.text = ""
            }.await()
        }
    }

    fun onStart() {
        when (viewModel.shownJob()) {
            null -> {
                showAlert("No job selected!", "You must save & select your plot job before you run it.")
            }
            else -> {
                viewModel.shownJob()?.start()
            }
        }
    }

    fun onStop() {
        val job: PlotJob = viewModel.shownJob() ?: return
        if (job.state.running && showConfirmation("Stop Process", "Are you sure you want to stop $job?")) {
            job.stop()
        }
    }
}
