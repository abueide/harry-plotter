package com.abysl.harryplotter.view

import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.model.PlotJob
import com.abysl.harryplotter.model.DataModel.chia
import com.abysl.harryplotter.model.DataModel.jobs
import com.abysl.harryplotter.model.DataModel.jobsFlow
import com.abysl.harryplotter.model.DataModel.selectedJob
import com.abysl.harryplotter.util.limitToInt
import com.abysl.harryplotter.viewmodel.JobEditorViewModel
import com.abysl.harryplotter.viewmodel.JobsListViewModel
import com.abysl.harryplotter.viewmodel.MainViewModel
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
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException

class JobsListView {

    @FXML
    lateinit var jobsView: ListView<PlotJob>

    @FXML
    private lateinit var stagger: TextField

    private var staggerRoutines: MutableList<Job> = mutableListOf()

    lateinit var viewModel: JobsListViewModel
    lateinit var mainViewModel: MainViewModel

    fun initialized(mainViewModel: MainViewModel) {
        this.mainViewModel = mainViewModel
        this.viewModel =

        jobsFlow
            .onEach {
                Platform.runLater {
                    jobsView.items.setAll(it)
                    jobsView.selectionModel.select(selectedJob)
                }
            }
        jobsView.selectionModel.selectedItemProperty().addListener { obs, old, new ->
            selectedJob = new
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
            jobs.forEach { it.stop() }
        }
    }

    val jobsMenu = ContextMenu()

    val duplicate = MenuItem("Duplicate").also {
        it.setOnAction {
            val job = selectedJob ?: return@setOnAction
            jobs += PlotJob(chia, job.jobDesc)
        }
        jobsMenu.items.add(it)
    }
    val delete = MenuItem("Delete").also {
        it.setOnAction {
            val job = selectedJob ?: return@setOnAction
            if (showConfirmation("Delete Job?", "Are you sure you want to delete ${job.jobDesc}")) {
                jobs -= job
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
        jobs.forEach {
            it.start()
            delay(Prefs.stagger * MILLIS_PER_MINUTE)
        }
    }

    companion object {
        private const val MILLIS_PER_MINUTE = 60000
    }
}
