package com.abysl.harryplotter.controller

import com.abysl.harryplotter.data.JobProcess
import com.abysl.harryplotter.util.unlines
import com.abysl.harryplotter.windows.SimpleDialogs.showAlert
import com.abysl.harryplotter.windows.SimpleDialogs.showConfirmation
import javafx.application.Platform
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.MultipleSelectionModel
import javafx.scene.control.TextArea
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JobStatusController {

    @FXML
    private lateinit var logsWindow: TextArea

    private lateinit var jobs: ObservableList<JobProcess>
    private lateinit var selectedJob: MultipleSelectionModel<JobProcess?>

    fun initModel(jobs: ObservableList<JobProcess>, selectedJob: MultipleSelectionModel<JobProcess?>) {
        this.jobs = jobs
        this.selectedJob = selectedJob
        selectedJob.selectedItemProperty().addListener { observable, old, new ->
            logsWindow.clear()
            old?.logs?.removeListener(logListener)
            new?.logs?.let {
                it.addListener(logListener)
                logsWindow.text = it.unlines()
            }
        }
    }

    fun onStart() {
        when {
            jobs.isEmpty() -> {
                showAlert("No plot jobs found!", "You must save & select your plot job before you run it.")
            }
            selectedJob.selectedItem == null -> {
                showAlert("No job selected!", "You must save & select your plot job before you run it.")
            }
            else -> {
                CoroutineScope(Dispatchers.Default).launch {
                    selectedJob.selectedItem?.start()
                }
            }
        }
    }

    fun onStop() {
        val job = selectedJob.selectedItem
        if (job == null) {
            println("Selected job is null!!!")
            return
        }
        if (showConfirmation("Stop Process", "Are you sure you want to stop ${job.jobDesc}?")) {
            job.stop()
        }
    }

    val logListener = ListChangeListener<String> {
        Platform.runLater {
            while (it.next()) {
                if (it.wasAdded()) {
                    logsWindow.appendText(it.addedSubList.unlines())
                } else {
                    logsWindow.text = it.list.unlines()
                }
            }
        }
    }
}
