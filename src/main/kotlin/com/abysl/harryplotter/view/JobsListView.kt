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

    @FXML
    private lateinit var stagger: TextField

    private var staggerRoutines: MutableList<Job> = mutableListOf()

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
        stagger.limitToInt()
        stagger.text = Prefs.stagger.toString()
        stagger.textProperty().addListener(staggerListener)
    }

    fun onStartAll() {
        staggerRoutines.add(staggerRoutine())
    }

    fun onStopAll() {
        if (showConfirmation("Stop Processes", "Are you sure you want to stop all plots?")) {
            staggerRoutines.forEach { it.cancel(CancellationException("User Stopped All")) }
            viewModel.plotJobs().forEach { it.stop() }
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

    private val staggerListener = ChangeListener<String> { _, _, new ->
        if (new.isBlank()) {
            Prefs.stagger = 0
        } else {
            new.toLongOrNull()?.let { Prefs.stagger = it }
        }
    }

    fun staggerRoutine() = CoroutineScope(Dispatchers.Default).launch {
        viewModel.plotJobs().forEach {
            if (!it.state.running) {
                it.start()
                delay(Prefs.stagger * MILLIS_PER_MINUTE)
            }
        }
    }

    companion object {
        private const val MILLIS_PER_MINUTE = 60000
    }
}
