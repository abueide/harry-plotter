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

package com.abysl.harryplotter.view

import com.abysl.harryplotter.chia.ChiaCli
import com.abysl.harryplotter.chia.ChiaLocator
import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.util.invoke
import com.abysl.harryplotter.viewmodel.MainViewModel
import com.abysl.harryplotter.windows.VersionPromptWindow
import javafx.application.HostServices
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.layout.VBox
import javafx.stage.Stage

class MainView {
    // UI Components ---------------------------------------------------------------------------------------------------
    @FXML
    private lateinit var mainBox: VBox

    @FXML
    private lateinit var jobsListViewController: JobsListView

    @FXML
    private lateinit var jobEditorViewController: JobEditorView

    @FXML
    private lateinit var jobStatusViewController: JobStatusView

    lateinit var hostServices: HostServices
    lateinit var toggleTheme: () -> Unit
    lateinit var viewModel: MainViewModel


    fun initialized() {
        viewModel = MainViewModel(findChia())
        jobsListViewController.initialized(viewModel.jobsListViewModel)
        jobEditorViewController.initialized(viewModel.jobEditorViewModel)
        jobStatusViewController.initialized(viewModel.jobStatusViewModel)
    }

    // Calls after the JavaFX vars are populated so they aren't null

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
        val jobs = viewModel.jobsListViewModel.plotJobs()
        if (jobs.any { it.state.running() }) {
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

    fun findChia(): ChiaCli {
        val chiaLocator = ChiaLocator(mainBox)
        val exePath = chiaLocator.getExePath()
        Prefs.exePath = exePath.path
        return ChiaCli(exePath, chiaLocator.getConfigFile())
    }


}
