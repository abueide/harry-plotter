/*
 *     Copyright (c) 2021 Andrew Bueide
 *
 *     This file is part of Harry Plotter.
 *
 *     Harry Plotter is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Harry Plotter is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Harry Plotter.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.abysl.harryplotter.ui.jobs

import com.abysl.harryplotter.config.Config
import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.model.PlotJob
import com.abysl.harryplotter.util.invoke
import com.abysl.harryplotter.ui.jobs.JobsListViewModel
import com.abysl.harryplotter.ui.all.SimpleDialogs.showConfirmation
import com.abysl.harryplotter.ui.all.SimpleDialogs.showOptions
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListView
import javafx.scene.control.MenuItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val GRACEFUL_STOP = "Graceful Stop"
private const val FORCE_STOP = "Force Stop"
private const val PERCENTAGE_REFRESH_DELAY = 100L

class JobsListView {

    @FXML
    lateinit var jobsView: ListView<PlotJob>

    lateinit var viewModel: JobsListViewModel

    val stateRefreshScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    fun initialized(jobsListViewModel: JobsListViewModel) {
        this.viewModel = jobsListViewModel.also { it.refreshCallback = jobsView::refresh }
        stateRefreshScope.launch {
            while (true) {
                Platform.runLater { jobsView.refresh() }
                delay(PERCENTAGE_REFRESH_DELAY)
            }
        }
        jobsListViewModel.plotJobs.onEach { jobList ->
            Platform.runLater {
                jobsView.items.setAll(jobList)
                jobsView.selectionModel.select(viewModel.selectedPlotJob())
            }
        }.launchIn(CoroutineScope(Dispatchers.IO))
        jobsListViewModel.selectedPlotJob.onEach {
            if (it != null) {
                jobsView.selectionModel.select(it)
            } else {
                jobsView.selectionModel.clearSelection()
            }
        }.launchIn(CoroutineScope(Dispatchers.IO))
        jobsView.selectionModel.selectedItemProperty().addListener { obs, old, new ->
            viewModel.selectedPlotJob.value = new
        }
        jobsView.contextMenu = jobsMenu

        if (Prefs.startStaggerManager) {
            onStartAll()
        }
    }

    fun onStartAll() {
        viewModel.onStartAll()
    }

    fun onStopAll() {
        if (viewModel.plotJobs.value.none { it.isRunning() }) return
        showOptions("Are you sure?", GRACEFUL_STOP, FORCE_STOP) {
            when (it) {
                FORCE_STOP -> viewModel.forceStopAll()
                GRACEFUL_STOP -> viewModel.gracefulStopAll()
            }
        }
    }

    fun onClear() {
        viewModel.onClear()
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
