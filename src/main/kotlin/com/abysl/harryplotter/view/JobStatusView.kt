package com.abysl.harryplotter.view

import com.abysl.harryplotter.model.PlotJob
import com.abysl.harryplotter.util.bindings.bind
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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
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

    private suspend fun <T> Flow<T>.asStringFlow(): StateFlow<String> {
        return this.map { it.toString() }.stateIn(jobBinding)
    }

    fun bind(plotJob: PlotJob?) {
        unbind()
        plotJob ?: return
        runBlocking {
            plotJob.stats.let { stats ->
                totalPlotsCreated.textProperty().bind(stats.plotsDoneFlow.asStringFlow())
                lastPlotTime.textProperty().bind(stats.lastPlotTime.asStringFlow())
                averagePlotTime.textProperty().bind(stats.averagePlotTime.asStringFlow())
                estimatedPlotsDay.textProperty().bind(stats.estimatedPlotsDay.asStringFlow())
                averagePlotsDay.textProperty().bind(stats.averagePlotsDay.asStringFlow())
            }
        }
    }


    fun unbind() {
        jobBinding.cancel()
        jobBinding = CoroutineScope(Dispatchers.IO)
        currentStatus.text = ""
        plotId.text = ""
        lastPlotTime.text = ""
        totalPlotsCreated.text = ""
        averagePlotTime.text = ""
        estimatedPlotsDay.text = ""
        averagePlotsDay.text = ""
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
        if (showConfirmation("Stop Process", "Are you sure you want to stop $job?")) {
            job.stop()
        }
    }
}
