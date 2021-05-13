package com.abysl.harryplotter.controller

import com.abysl.harryplotter.chia.ChiaCli
import com.abysl.harryplotter.data.JobProcess
import com.abysl.harryplotter.windows.SimpleDialogs
import com.abysl.harryplotter.windows.SimpleDialogs.showConfirmation
import javafx.beans.value.ChangeListener
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListView
import javafx.scene.control.MenuItem
import javafx.scene.control.MultipleSelectionModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import java.util.*

class JobsListController : Initializable {

    @FXML
    lateinit var jobsView: ListView<JobProcess>

    private lateinit var chia: ChiaCli

    private lateinit var jobs: ObservableList<JobProcess>


    override fun initialize(location: URL?, resources: ResourceBundle?) { }

    fun initModel(
        chia: ChiaCli,
        jobs: ObservableList<JobProcess>
    ): MultipleSelectionModel<JobProcess?> {
        jobsView.items = jobs
        this.jobs = jobs
        this.chia = chia
        jobsView.contextMenu = jobsMenu
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
        if (showConfirmation("Stop Processes", "Are you sure you want to stop all plots?")) {
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
}
