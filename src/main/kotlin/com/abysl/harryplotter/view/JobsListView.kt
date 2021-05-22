package com.abysl.harryplotter.view

import com.abysl.harryplotter.config.Config
import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.model.PlotJob
import com.abysl.harryplotter.util.invoke
import com.abysl.harryplotter.util.limitToInt
import com.abysl.harryplotter.viewmodel.JobsListViewModel
import com.abysl.harryplotter.windows.SimpleDialogs.showConfirmation
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.fxml.FXML
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListView
import javafx.scene.control.MenuItem
import javafx.scene.control.TextField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException

class JobsListView {

    @FXML
    lateinit var jobsView: ListView<PlotJob>

    lateinit var viewModel: JobsListViewModel

    fun initialized(jobsListViewModel: JobsListViewModel) {
        this.viewModel = jobsListViewModel

        jobsListViewModel.plotJobs.onEach {
            Platform.runLater {
                jobsView.items.setAll(it)
                jobsView.selectionModel.select(viewModel.selectedPlotJob())
            }
        }.launchIn(CoroutineScope(Dispatchers.IO))
        jobsView.selectionModel.selectedItemProperty().addListener { obs, old, new ->
            viewModel.selectedPlotJob.value = new
        }
        jobsView.contextMenu = jobsMenu
    }

    fun onStartAll() {
        viewModel.onStartAll()
    }

    fun onStopAll() {
        if (showConfirmation("Stop Processes", "Are you sure you want to stop all plots?")) {
            viewModel.onStopAll()
        }
    }

    val jobsMenu = ContextMenu()

    val duplicate = MenuItem("Duplicate").also {
        it.setOnAction {
            val job = viewModel.selectedPlotJob() ?: return@setOnAction
            viewModel.plotJobs.value += PlotJob(job.description)
            Config.savePlotJobs(viewModel.plotJobs.value)
        }
        jobsMenu.items.add(it)
    }

    val delete = MenuItem("Delete").also {
        it.setOnAction {
            val job = viewModel.selectedPlotJob() ?: return@setOnAction
            if (showConfirmation("Delete Job?", "Are you sure you want to delete ${job.description}")) {
                viewModel.plotJobs.value -= job
                Config.savePlotJobs(viewModel.plotJobs.value)
            }
        }
        jobsMenu.items.add(it)
    }
}
