package com.abysl.harryplotter

import com.abysl.harryplotter.chia.ChiaCli
import com.abysl.harryplotter.config.Config
import com.abysl.harryplotter.controller.AddKey
import com.abysl.harryplotter.data.ChiaKey
import com.abysl.harryplotter.data.JobDescription
import com.abysl.harryplotter.data.JobProcess
import com.abysl.harryplotter.util.FxUtil
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import java.net.URL
import java.util.*
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
    private lateinit var selectTempDir: Button

    @FXML
    private lateinit var destDir: TextField

    @FXML
    private lateinit var threads: TextField

    @FXML
    private lateinit var ram: TextField

    @FXML
    private lateinit var chiaKeysCombo: ComboBox<ChiaKey>

    @FXML
    private lateinit var save: Button

    @FXML
    private lateinit var stopAll: Button

    @FXML
    private lateinit var startAll: Button

    @FXML
    private lateinit var currentStatus: Label

    @FXML
    private lateinit var lastPlotTime: Label

    @FXML
    private lateinit var averagePlotTime: Label

    @FXML
    private lateinit var totalPlotsCreated: Label

    @FXML
    private lateinit var estimatedPlotsDay: Label

    @FXML
    private lateinit var averagePlotsDay: Label

    @FXML
    private lateinit var stop: Button

    @FXML
    private lateinit var start: Button

    @FXML
    private lateinit var addKey: Button

    @FXML
    private lateinit var themeToggle: Button

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
        chiaKeysCombo.selectionModel.selectFirst()

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
        chia = ChiaCli(getExePath(), getConfigFile())
        chiaKeysCombo.items.addAll(chia.readKeys())
        chiaKeysCombo.selectionModel.selectFirst()
        jobs.addAll(Config.getPlotJobs().map { JobProcess(chia, logsWindow, it) })
        jobs.addListener(ListChangeListener {
            Config.savePlotJobs(jobs.map { it.jobDesc })
        })

        jobsView.items = jobs
        jobsView.contextMenu = jobsMenu
        jobsView.selectionModel.selectedItemProperty().addListener {observable, oldvalue, newvalue ->
            oldvalue?.displayLogs = false
            logsWindow.clear()
            newvalue?.displayLogs = true
            newvalue?.let {
                loadJob(it.jobDesc)
            }
        }
        jobsView.selectionModel.selectFirst()
    }

    // User Actions ----------------------------------------------------------------------------------------------------

    fun onStartAll() {
        jobsView.items.forEach { it.start() }
    }

    fun onStart() {
        if(jobs.isEmpty()){
            showAlert("No plot jobs found!", "You must save & select your plot job before you run it.")
        }else {
            jobsView.selectionModel.selectedItem.start()
        }
    }

    fun onStop() {
        val job = jobsView.selectionModel.selectedItem
        if (showConfirmation("Stop Process", "Are you sure you want to stop ${job.jobDesc}?")) {
            job.reset()
        }
    }

    fun onStopAll() {
        if (showConfirmation("Stop Processes", "Are you sure you want to stop all plots?")) {
            jobs.forEach { it.reset() }
        }
    }

    fun onTempBrowse() {
        chooseDir("Select Temp Dir", false)?.let {
            tempDir.text = it.absolutePath
        }
    }

    fun onDestBrowse() {
        chooseDir("Select Destination Dir", false)?.let {
            destDir.text = it.absolutePath
        }
    }

    fun onSave() {
        if (tempDir.text.isBlank() || destDir.text.isBlank()) {
            showAlert("Directory Not Selected", "Please make sure to select a destination & temporary directory.")
            return
        }
        val temp = File(tempDir.text)
        val dest = File(destDir.text)
        if (!temp.exists()) {
            showAlert("Temp Directory Does Not Exist", "Please select a valid directory.")
            return
        }
        if (!dest.exists()) {
            showAlert("Destination Directory Does Not Exist", "Please select a valid directory.")
            return
        }
        if (!temp.isDirectory) {
            showAlert("Selected Temp Is Not A Directory", "Please select a valid directory.")
            return
        }
        if (!dest.isDirectory) {
            showAlert("Selected Destination Is Not A Directory", "Please select a valid directory.")
            return
        }
        val name = jobName.text.ifBlank { "Plot Job ${jobs.count() + 1}" }
        val key = chiaKeysCombo.selectionModel.selectedItem
        jobs.add(
            JobProcess(
                chia, logsWindow,
                JobDescription(
                    name, File(tempDir.text), File(destDir.text),
                    threads.text.ifBlank { "2" }.toInt(),
                    ram.text.ifBlank { "4608" }.toInt(),
                    key,
                    plotsToFinish.text.ifBlank { "0" }.toInt()
                )
            )
        )
    }

    fun onCancel() {
        jobName.clear()
        tempDir.clear()
        destDir.clear()
        threads.clear()
        ram.clear()
        chiaKeysCombo.selectionModel.selectFirst()
        jobsView.selectionModel.clearSelection()
    }

    fun onAddKey() {
        AddKey(keys).show()
    }

    fun onToggleTheme() {
        toggleTheme()
    }

    fun onStopAfter() {
        plotsToFinish.disableProperty().value = !stopAfterCheckBox.selectedProperty().value
    }

    fun onExit() {
        if (jobs.any { it.running }) {
            val alert = Alert(Alert.AlertType.CONFIRMATION)
            alert.title = "Let plot jobs finish?"
            alert.headerText = "Let plot jobs finish?"
            alert.contentText = "Would you like to let plot jobs finish or close them?"
            (alert.dialogPane.lookupButton(ButtonType.OK) as Button).text = "Let them finish"
            (alert.dialogPane.lookupButton(ButtonType.CANCEL) as Button).text = "Close them"
            val answer = alert.showAndWait()
            if (answer.get() != ButtonType.OK) {
                jobs.forEach {
                    it.reset()
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
        var chiaExe = File(System.getProperty("user.home") + "/AppData/Local/chia-blockchain/")

        if (chiaExe.exists()) {
            chiaExe.list().forEach {
                if (it.contains("app-")) {
                    chiaExe = File(chiaExe.path + "/$it/resources/app.asar.unpacked/daemon/chia.exe")
                    return chiaExe
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

    fun chooseFile(title: String): File {
        return chooseFile(title, FileChooser.ExtensionFilter("All", "*.*"))
    }

    fun chooseFile(title: String, vararg extensions: FileChooser.ExtensionFilter): File {
        val fileChooser = FileChooser()
        fileChooser.title = title
        fileChooser.extensionFilters.addAll(extensions)
        val file: File? = fileChooser.showOpenDialog(mainBox.scene.window)
        if (file != null) {
            return file
        }
        if (!showConfirmation("File Not Selected", "Try again?")) {
            exitProcess(0)
        }
        return chooseFile(title, *extensions)
    }

    fun chooseDir(title: String, required: Boolean = false): File? {
        val directoryChooser = DirectoryChooser()
        directoryChooser.title = title
        val directory: File? = directoryChooser.showDialog(mainBox.scene.window)
        if (required) {
            showAlert("Directory Not Selected", "Please try again.")
            return chooseDir(title)
        }
        return directory
    }

    fun showAlert(title: String, content: String) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = title
        alert.headerText = title
        alert.contentText = content
        FxUtil.setTheme(alert.dialogPane.scene)
        alert.showAndWait()
    }

    fun showConfirmation(title: String, content: String): Boolean {
        val alert = Alert(Alert.AlertType.CONFIRMATION)
        alert.title = title
        alert.headerText = title
        alert.contentText = content
        FxUtil.setTheme(alert.dialogPane.scene)
        val answer = alert.showAndWait()
        return answer.get() == ButtonType.OK
    }

    fun loadJob(jobDesc: JobDescription){
        jobName.text = jobDesc.name
        tempDir.text = jobDesc.tempDir.path
        destDir.text = jobDesc.destDir.path
        threads.text = jobDesc.threads.toString()
        ram.text = jobDesc.ram.toString()
        plotsToFinish.text = jobDesc.plotsToFinish.toString()
        chiaKeysCombo.selectionModel.select(jobDesc.key)
    }
}


