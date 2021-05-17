package com.abysl.harryplotter.view

import com.abysl.harryplotter.model.DataModel.jobs
import com.abysl.harryplotter.model.DataModel.selectedJob
import com.abysl.harryplotter.model.DataModel.selectedJobFlow
import com.abysl.harryplotter.windows.SimpleDialogs.showAlert
import com.abysl.harryplotter.windows.SimpleDialogs.showConfirmation
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.TextArea
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

class JobStatusView {

    @FXML
    private lateinit var logsWindow: TextArea

    var logsScope = CoroutineScope(Dispatchers.IO)

    fun initialized() {
        selectedJobFlow
            .onEach { job ->
                logsScope.cancel()
                logsWindow.clear()
                val current = job ?: return@onEach
                logsScope = CoroutineScope(Dispatchers.IO)
                current.logsFlow
                    .onEach { logs -> Platform.runLater { logsWindow.text = logs.unlines() } }
                    .launchIn(logsScope)
            }.launchIn(CoroutineScope(Dispatchers.IO))
    }

    fun onStart() {
        when {
            jobs.isEmpty() -> {
                showAlert("No plot jobs found!", "You must save & select your plot job before you run it.")
            }
            selectedJob == null -> {
                showAlert("No job selected!", "You must save & select your plot job before you run it.")
            }
            else -> {
                selectedJob?.start()
            }
        }
    }

    fun onStop() {
        val job = selectedJob
        if (job == null) {
            println("Selected job is null!!!")
            return
        }
        if (showConfirmation("Stop Process", "Are you sure you want to stop ${job.jobDesc}?")) {
            job.stop()
        }
    }
}
