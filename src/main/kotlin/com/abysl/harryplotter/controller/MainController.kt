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
import com.abysl.harryplotter.config.Config
import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.data.ChiaKey
import com.abysl.harryplotter.data.JobProcess
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListView
import javafx.scene.control.MenuItem
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL
import java.util.ResourceBundle
import kotlin.system.exitProcess

class MainController : Initializable {
    // UI Components ---------------------------------------------------------------------------------------------------
    @FXML
    private lateinit var mainBox: VBox

    @FXML
    private lateinit var jobsView: ListView<JobProcess>

    @FXML
    private lateinit var jobName: TextField

    @FXML
    private lateinit var tempDir: TextField

    @FXML
    private lateinit var destDir: TextField

    @FXML
    private lateinit var threads: TextField

    @FXML
    private lateinit var ram: TextField

    @FXML
    private lateinit var chiaKeysCombo: ComboBox<ChiaKey>

    @FXML
    private lateinit var stopAfterCheckBox: CheckBox

    @FXML
    private lateinit var plotsToFinish: TextField

    @FXML
    private lateinit var logsWindow: TextArea

    lateinit var chia: ChiaCli
    lateinit var toggleTheme: () -> Unit

    val jobs: ObservableList<JobProcess> = FXCollections.observableArrayList()
    val keys: ObservableList<ChiaKey> = FXCollections.observableArrayList()

    // Initial State ---------------------------------------------------------------------------------------------------

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        chiaKeysCombo.items = keys
        keys.add(Config.devkey)

        threads.textProperty().addListener { observable, oldValue, newValue ->
            if (!newValue.matches(Regex("\\d*"))) {
                threads.text = newValue.replace("[^\\d]".toRegex(), "")
            }
        }

        ram.textProperty().addListener { observable, oldValue, newValue ->
            if (!newValue.matches(Regex("\\d*"))) {
                ram.text = newValue.replace("[^\\d]".toRegex(), "")
            }
        }

        plotsToFinish.textProperty().addListener { observable, oldValue, newValue ->
            if (!newValue.matches(Regex("\\d*"))) {
                plotsToFinish.text = newValue.replace("[^\\d]".toRegex(), "")
            }
        }
    }

    // Calls after the window is initialized so mainBox.scene.window isn't null
    fun initialized() {
        val exePath = getExePath()
        Prefs.exePath = exePath.path
        chia = ChiaCli(getExePath(), getConfigFile())
        chiaKeysCombo.items.addAll(chia.readKeys())
        chiaKeysCombo.selectionModel.selectFirst()
        jobs.addAll(Config.getPlotJobs().map { JobProcess(chia, logsWindow, it) })
        jobs.addListener(
            ListChangeListener {
                Config.savePlotJobs(jobs.map { it.jobDesc })
            }
        )

        jobsView.items = jobs
        jobsView.contextMenu = jobsMenu
        jobsView.selectionModel.selectedItemProperty().addListener { observable, oldvalue, newvalue ->
            oldvalue?.state?.displayLogs = false
            newvalue?.state?.displayLogs = true
            newvalue?.let {
                loadJob(it)
            }
        }
        jobsView.selectionModel.selectFirst()
    }

    // User Actions ----------------------------------------------------------------------------------------------------

    fun onStartAll() {
        jobsView.items.forEach {
            CoroutineScope(Dispatchers.Default).launch {
                it.start()
            }
        }
    }

    fun onStart() {
        if (jobs.isEmpty()) {
            showAlert("No plot jobs found!", "You must save & select your plot job before you run it.")
        } else {
            jobsView.selectionModel.selectedItem.start()
        }
    }

    fun onStop() {
        val job = jobsView.selectionModel.selectedItem
        if (showConfirmation("Stop Process", "Are you sure you want to stop ${job.jobDesc}?")) {
            job.stop()
        }
    }

    fun onStopAll() {
        if (showConfirmation("Stop Processes", "Are you sure you want to stop all plots?")) {
            jobs.forEach { it.stop() }
        }
    }

    fun onAddKey() {
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

        close()
    }

    // GUI Components --------------------------------------------------------------------------------------------------
    val jobsMenu = ContextMenu()
    val duplicate = MenuItem("Duplicate").also {
        it.setOnAction {
            val jobProc = jobsView.selectionModel.selectedItem
            jobs.add(JobProcess(chia, logsWindow, jobProc.jobDesc))
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

    // Utility Functions -----------------------------------------------------------------------------------------------

    private fun close() {
        val stage = mainBox.scene.window as Stage
        stage.close()
    }

    fun getConfigFile(): File {
        val configDir = File(System.getProperty("user.home") + "/.chia/mainnet/config/config.yaml")
        if (configDir.exists()) {
            return configDir
        }
        showAlert(
            "Chia Config File Not Found",
            "Please specify the chia config location, usually located at C:\\Users\\YourUser\\.chia\\mainnet\\config\\config.yaml"
        )
        val file = chooseFile(
            "Select Chia Config File",
            FileChooser.ExtensionFilter("YAML Config File", "config.yaml")
        )

        if (file.name.equals("config.yaml") && file.exists())
            return file
        else {
            if (showConfirmation(
                    "Wrong File",
                    "Looking for config.yaml, usually located at C:\\Users\\YourUser\\.chia\\mainnet\\config\\config.yaml . Try again?"
                )
            ) {
                return getConfigFile()
            } else {
                exitProcess(0)
            }
        }
    }

    fun getExePath(): File {
        val lastPath = File(Prefs.exePath)
        if (lastPath.exists()) return lastPath
        val macChiaExe = File("/Applications/Chia.app/Contents/Resources/app.asar.unpacked/daemon/chia")
        if (macChiaExe.exists()) return macChiaExe

        var chiaAppData = File(System.getProperty("user.home") + "/AppData/Local/chia-blockchain/")

        if (chiaAppData.exists()) {
            chiaAppData.list()?.forEach {
                if (it.contains("app-")) {
                    chiaAppData = File(chiaAppData.path + "/$it/resources/app.asar.unpacked/daemon/chia.exe")
                    return chiaAppData
                }
            }
        }
        showAlert("Chia Executable Not Found", "Please specify the chia executable location")
        val file = chooseFile(
            "Select Chia Executable",
            FileChooser.ExtensionFilter("All Files", "*.*"),
            FileChooser.ExtensionFilter("Executable File", "*.exe")
        )

        if (file.name.startsWith("chia"))
            return file
        else {
            if (showConfirmation(
                    "Wrong File",
                    "Looking for the chia cli executable (chia.exe lowercase). Try again?"
                )
            ) {
                return getExePath()
            } else {
                exitProcess(0)
            }
        }
    }

    fun loadJob(jobProc: JobProcess) {
        val jobDesc = jobProc.jobDesc
        jobName.text = jobDesc.name
        tempDir.text = jobDesc.tempDir.path
        destDir.text = jobDesc.destDir.path
        threads.text = jobDesc.threads.toString()
        ram.text = jobDesc.ram.toString()
        plotsToFinish.text = jobDesc.plotsToFinish.toString()
        chiaKeysCombo.selectionModel.select(jobDesc.key)
        logsWindow.text = jobProc.getLogsAsString()
        // Makes the textarea scroll to the bottom by default
        logsWindow.appendText(" ")
    }
}
