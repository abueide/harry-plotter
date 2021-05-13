package com.abysl.harryplotter.controller

import com.abysl.harryplotter.data.JobProcess
import com.abysl.harryplotter.windows.SimpleDialogs
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ListView
import javafx.scene.control.MultipleSelectionModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import java.util.*

class JobsListController : Initializable {

    @FXML
    lateinit var jobsView: ListView<JobProcess>

    private val dialogs = SimpleDialogs()

    private lateinit var jobs: ObservableList<JobProcess>

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        jobsView.items = jobs
    }

    fun initModel(
        jobs: ObservableList<JobProcess>,
    ): MultipleSelectionModel<JobProcess?> {
        jobsView.items = jobs
        this.jobs = jobs
        return jobsView.selectionModel
    }

    fun onStartAll() {
        jobs.forEach {
            CoroutineScope(Dispatchers.Default).launch {
                it.start()
            }
        }
    }

    fun onStopAll() {
        if (dialogs.showConfirmation("Stop Processes", "Are you sure you want to stop all plots?")) {
            jobs.forEach { it.stop() }
        }
    }
}
