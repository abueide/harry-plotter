package com.abysl.harryplotter.view

import com.abysl.harryplotter.model.PlotJob
import com.abysl.harryplotter.util.formatted
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import java.time.Duration


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

    var jobBinding: CoroutineScope = CoroutineScope(Dispatchers.IO)
    var fxBinding: CoroutineScope = CoroutineScope(Dispatchers.JavaFx)

    fun initialized(jobStatusViewModel: JobStatusViewModel) {
        this.viewModel = jobStatusViewModel
        viewModel.shownJob.onEach(::bind).launchIn(CoroutineScope(Dispatchers.IO))
    }

    fun bind(plotJob: PlotJob?) {
        unbind()
        plotJob ?: return
        plotJob.statsFlow.onEach {
            fxBinding.launch {
                totalPlotsCreated.text = it.plotsDone.toString()
                lastPlotTime.text = Duration.ofSeconds(it.lastPlotTime.toLong()).formatted()
                averagePlotTime.text = Duration.ofSeconds(it.averagePlotTime.toLong()).formatted()
                estimatedPlotsDay.text = it.estimatedPlotsDay.toString()
                val result = it.results.lastOrNull()
                if (result != null) {
                    p1Time.text = Duration.ofSeconds(result.phaseOneTime.toLong()).formatted()
                    p2Time.text = Duration.ofSeconds(result.phaseTwoTime.toLong()).formatted()
                    p3Time.text = Duration.ofSeconds(result.phaseThreeTime.toLong()).formatted()
                    p4Time.text = Duration.ofSeconds(result.phaseFourTime.toLong()).formatted()
                    copyTime.text = Duration.ofSeconds(result.copyTime.toLong()).formatted()
                }
            }
        }.launchIn(jobBinding)
        plotJob.process?.state?.onEach {
            fxBinding.launch {
                currentStatus.text = it.status
                plotId.text = it.plotId
            }
        }?.launchIn(jobBinding)
        plotJob.process?.newLogs?.onEach {
            fxBinding.launch {
                logsWindow.appendText(it.unlines())
            }
        }?.launchIn(jobBinding)
        logsWindow.appendText(plotJob.process?.logFile?.readText() ?: "")
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
                viewModel.shownJob()?.also { it.tempDone = 0 }?.start(true)
            }
        }
    }

    fun onStop() {
        val job: PlotJob = viewModel.shownJob() ?: return
        if (job.isRunning() && showConfirmation("Stop Process", "Are you sure you want to stop $job?")) {
            job.stop()
        }
    }
}
