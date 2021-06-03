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

import HarryPlotterSettingsWindow
import com.abysl.harryplotter.chia.ChiaLocator
import com.abysl.harryplotter.config.Config
import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.util.invoke
import com.abysl.harryplotter.viewmodel.MainViewModel
import com.abysl.harryplotter.windows.ChiaSettingsWindow
import com.abysl.harryplotter.windows.ReleaseWindow
import com.abysl.harryplotter.windows.SimpleDialogs
import com.abysl.harryplotter.windows.StaggerSettingsWindow
import com.abysl.harryplotter.windows.VersionPromptWindow
import javafx.application.HostServices
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.kordamp.ikonli.fontawesome5.FontAwesomeRegular
import org.kordamp.ikonli.javafx.FontIcon

private const val GRACEFUL_STOP = "Graceful Stop"
private const val FORCE_STOP = "Force Stop"

class MainView {
    // UI Components ---------------------------------------------------------------------------------------------------
    @FXML
    private lateinit var mainBox: VBox

    @FXML
    private lateinit var themeToggle: Button

    @FXML
    private lateinit var jobsListViewController: JobsListView

    @FXML
    private lateinit var jobEditorViewController: JobEditorView

    @FXML
    private lateinit var jobStatusViewController: JobStatusView

    @FXML
    private lateinit var statsViewController: StatsView

    @FXML
    private lateinit var driveViewController: DriveView

    lateinit var hostServices: HostServices
    lateinit var toggleTheme: () -> Unit
    lateinit var viewModel: MainViewModel

    fun initialized(hostServices: HostServices) {
        this.hostServices = hostServices
        findChia()
        viewModel = MainViewModel()
        jobsListViewController.initialized(viewModel.jobsListViewModel)
        jobEditorViewController.initialized(viewModel.jobEditorViewModel)
        jobStatusViewController.initialized(viewModel.jobStatusViewModel)
        statsViewController.initialized(viewModel.statsViewModel)
        driveViewController.initialized(viewModel.driveViewModel)
        setButtonTheme()
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                Platform.runLater {
                    ReleaseWindow(hostServices).show()
                }
                delay(MS_PER_HOUR)
            }
        }
    }

    // Calls after the JavaFX vars are populated so they aren't null

    fun onAbout() {
        VersionPromptWindow.show()
    }

    fun onBugReport() {
        hostServices.showDocument("https://github.com/abueide/harry-plotter/issues/new")
    }

    fun onHarrySettings() {
        HarryPlotterSettingsWindow().show()
    }

    fun onChiaSettings() {
        ChiaSettingsWindow().show()
    }

    fun onStaggerSettings() {
        StaggerSettingsWindow().show(viewModel.staggerManager::updateGlobalStagger)
    }

    fun onToggleTheme() {
        toggleTheme()
        setButtonTheme()
    }

    fun onExit() {
        val jobs = jobsListViewController.viewModel.plotJobs.value
        if (jobs.any { it.isRunning() }) {
            val answer = SimpleDialogs.showOptionsBlocking("Are you sure?", GRACEFUL_STOP, FORCE_STOP)
            when (answer) {
                FORCE_STOP -> jobsListViewController.viewModel.forceStopAll(block = true)
                GRACEFUL_STOP -> jobsListViewController.viewModel.gracefulStopAll()
            }
        }
        Config.savePlotJobs(jobs)
        Config.saveDrives(viewModel.driveViewModel.drives())
        (mainBox.scene.window as Stage).close()
    }

    private val sun = FontIcon(FontAwesomeRegular.SUN).also {
        it.iconColor = Color.BLACK
        it.iconSize = 17
    }
    private val moon = FontIcon(FontAwesomeRegular.MOON).also {
        it.iconColor = Color.WHITE
        it.iconSize = 13
    }

    fun setButtonTheme() {
        if (Prefs.darkMode) {
            themeToggle.graphic = sun
            themeToggle.padding = Insets(0.0, 0.0, 0.0, 4.0)
        } else {
            themeToggle.graphic = moon
            themeToggle.padding = Insets(0.0, 0.0, 0.0, 3.0)
        }
    }

    fun findChia() {
        val chiaLocator = ChiaLocator(mainBox)
        Prefs.exePath = chiaLocator.getExePath().path
        Prefs.configPath = chiaLocator.getConfigFile().path
    }

    companion object {
        // 86400000
        private const val MS_PER_HOUR = 3600000L
    }
}
