package com.abysl.harryplotter.controller

import com.abysl.harryplotter.data.JobProcess
import com.abysl.harryplotter.windows.SimpleDialogs
import com.abysl.harryplotter.windows.SimpleDialogs.showAlert
import com.abysl.harryplotter.windows.SimpleDialogs.showConfirmation
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.Initializable
import javafx.scene.control.MultipleSelectionModel
import java.net.URL
import java.util.*

class JobStatusViewController : Initializable {

    private lateinit var jobs: ObservableList<JobProcess>
    private lateinit var selectedJob: MultipleSelectionModel<JobProcess?>

    override fun initialize(location: URL?, resources: ResourceBundle?) {

    }

    fun initModel(jobs: ObservableList<JobProcess>, selectedJob: MultipleSelectionModel<JobProcess?>){
        this.jobs = jobs
        this.selectedJob = selectedJob
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
                selectedJob.selectedItem?.start()
            }
        }
    }

    fun onStop() {
        val job = selectedJob.selectedItem ?: return
        if (showConfirmation("Stop Process", "Are you sure you want to stop ${job.jobDesc}?")) {
            job.stop()
        }
    }
}
