package com.abysl.harryplotter.controller

import com.abysl.harryplotter.chia.ChiaCli
import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.data.JobProcess
import com.abysl.harryplotter.util.limitToInt
import com.abysl.harryplotter.windows.SimpleDialogs.showConfirmation
import javafx.beans.value.ChangeListener
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListView
import javafx.scene.control.MenuItem
import javafx.scene.control.MultipleSelectionModel
import javafx.scene.control.TextField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException

class JobsListController {

    @FXML
    lateinit var jobsView: ListView<JobProcess>

    @FXML
    private lateinit var stagger: TextField

    private lateinit var chia: ChiaCli

    private lateinit var jobs: ObservableList<JobProcess>

    private var staggerRoutines: MutableList<Job> = mutableListOf()

    fun initModel(
        chia: ChiaCli,
        jobs: ObservableList<JobProcess>
    ): MultipleSelectionModel<JobProcess?> {
        jobsView.items = jobs
        jobsView.contextMenu = jobsMenu
        stagger.limitToInt()
        stagger.text = Prefs.stagger.toString()
        stagger.textProperty().addListener(staggerListener)
        this.jobs = jobs
        this.chia = chia
        return jobsView.selectionModel
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
            val jobProc = jobsView.selectionModel.selectedItem
            jobs.add(JobProcess(chia, jobProc.jobDesc))
        }
        jobsMenu.items.add(it)
    }
    val delete = MenuItem("Delete").also {
        it.setOnAction {
            val job = jobsView.selectionModel.selectedItem
            if (showConfirmation("Delete Job?", "Are you sure you want to delete ${job.jobDesc}")) {
                jobs.remove(job)
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
