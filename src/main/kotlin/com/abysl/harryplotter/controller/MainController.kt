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

package com.abysl.harryplotter.controller

import com.abysl.harryplotter.chia.ChiaCli
import com.abysl.harryplotter.chia.ChiaLocator
import com.abysl.harryplotter.config.Config
import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.data.ChiaKey
import com.abysl.harryplotter.data.JobProcess
import com.abysl.harryplotter.windows.VersionPromptWindow
import javafx.application.HostServices
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.MultipleSelectionModel
import javafx.scene.control.SingleSelectionModel
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.net.URL
import java.util.ResourceBundle

class MainController : Initializable {
    // UI Components ---------------------------------------------------------------------------------------------------
    @FXML
    private lateinit var mainBox: VBox

    @FXML
    private lateinit var jobsListController: JobsListController

    @FXML
    private lateinit var jobEditorController: JobEditorController

    @FXML
    private lateinit var jobStatusViewController: JobStatusController

    lateinit var chia: ChiaCli
    lateinit var hostServices: HostServices
    lateinit var toggleTheme: () -> Unit

    val jobs: ObservableList<JobProcess> = FXCollections.observableArrayList()
    val keys: ObservableList<ChiaKey> = FXCollections.observableArrayList()
    lateinit var selectedJob: MultipleSelectionModel<JobProcess?>
    lateinit var selectedKey: SingleSelectionModel<ChiaKey?>

    // Initial State ---------------------------------------------------------------------------------------------------

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        keys.add(Config.devkey)
    }

    // Calls after the window is initialized so mainBox.scene.window isn't null
    fun initialized() {
        val chiaLocator = ChiaLocator(mainBox)
        val exePath = chiaLocator.getExePath()
        Prefs.exePath = exePath.path
        chia = ChiaCli(exePath, chiaLocator.getConfigFile())

        selectedJob = jobsListController.initModel(chia, jobs)
        selectedKey = jobEditorController.initModel(jobs, keys, selectedJob)
        jobStatusViewController.initModel(jobs, selectedJob)

        jobs.addListener(jobsListListener)
        keys.addListener(keyListListener)

        keys.addAll(chia.readKeys())
        jobs.addAll(Config.getPlotJobs().map { JobProcess(chia, it) })
    }

    fun onAbout() {
        VersionPromptWindow.show()
    }

    fun onBugReport() {
        hostServices.showDocument("https://github.com/abueide/harry-plotter/issues/new")
    }

    fun onToggleTheme() {
        toggleTheme()
    }

    fun onExit() {
        if (jobs.any { it.state.running }) {
            val alert = Alert(Alert.AlertType.CONFIRMATION)
            alert.title = "Let plot jobs finish?"
            alert.headerText = "Let plot jobs finish?"
            alert.contentText = "Would you like to let plot jobs finish or close them?"
            (alert.dialogPane.lookupButton(ButtonType.OK) as Button).text = "Let them finish"
            (alert.dialogPane.lookupButton(ButtonType.CANCEL) as Button).text = "Close them"
            val answer = alert.showAndWait()
            if (answer.get() != ButtonType.OK) {
                jobs.forEach {
                    it.stop(true)
                }
            }
        }
        (mainBox.scene.window as Stage).close()
    }

    private val jobsListListener =
        ListChangeListener<JobProcess> { listChange ->
            while (listChange.next()) {
                if (listChange.wasAdded()) {
                    selectedJob.select(listChange.addedSubList.first())
                }
            }
            Config.savePlotJobs(jobs.map { it.jobDesc })
        }

    private val keyListListener = ListChangeListener<ChiaKey> {
        while (it.next()) {
            if (it.wasAdded()) {
                selectedKey.select(it.addedSubList.first())
            }
        }
    }
}
