package com.abysl.harryplotter

import com.abysl.harryplotter.chia.ChiaCli
import com.abysl.harryplotter.controller.AddKey
import com.abysl.harryplotter.data.ChiaKey
import com.abysl.harryplotter.data.Job
import com.abysl.harryplotter.data.Prefs
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
    private lateinit var jobsView: ListView<Job>

    @FXML
    private lateinit var statusJobsView: ListView<Job>

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
    private lateinit var chiaKeys: ComboBox<ChiaKey>

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

    lateinit var toggleTheme: () -> Unit

    val jobs: ObservableList<Job> = FXCollections.observableArrayList()
    val keys: ObservableList<ChiaKey> = FXCollections.observableArrayList()

    // Initial State ---------------------------------------------------------------------------------------------------
    lateinit var chia: ChiaCli

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        chiaKeys.items = keys
        chiaKeys.items.addListener(ListChangeListener {
            // Auto Select key if you just added first one.
            if (chiaKeys.items.size == 1) {
                chiaKeys.selectionModel.selectFirst()
            }
        })
    }

    // Calls after the window is initialized so mainBox.scene.window isn't null
    fun initialized() {
        chia = ChiaCli(getExePath(), getConfigFile())
        chiaKeys.items.addAll(chia.readKeys())
        chiaKeys.selectionModel.selectFirst()
    }

    // User Actions ----------------------------------------------------------------------------------------------------

    fun onStartAll() {

    }

    fun onStart() {
        chia.createPlot(statusJobsView.selectionModel.selectedItem)
    }

    fun onAddKey() {
        AddKey(keys).show()
    }

    fun onToggleTheme(){
        toggleTheme()
    }

    fun onExit(){
//        if (jobs.any { it.running }) {

            val alert = Alert(Alert.AlertType.CONFIRMATION)
            alert.title = "Let plot jobs finish?"
            alert.headerText = "Let plot jobs finish?"
            alert.contentText = "Would you like to let plot jobs finish or close them?"
            (alert.dialogPane.lookupButton(ButtonType.OK) as Button).text = "Let them finish"
            (alert.dialogPane.lookupButton(ButtonType.CANCEL) as Button).text = "Close them"
            val answer = alert.showAndWait()
            if(answer.get() != ButtonType.OK){
                jobs.forEach {
                    it.stop();
                }
            }
//        }

        close()
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

    fun chooseDir(title: String): File {
        val directoryChooser = DirectoryChooser()
        directoryChooser.title = title
        val directory: File? = directoryChooser.showDialog(mainBox.scene.window)
        if (directory != null)
            return directory
        showAlert("Directory Not Selected", "Please try again.")
        return chooseDir(title)
    }

    fun showAlert(title: String, content: String) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = title
        alert.headerText = title
        alert.contentText = content
        alert.showAndWait()
    }

    fun showConfirmation(title: String, content: String): Boolean {
        val alert = Alert(Alert.AlertType.CONFIRMATION)
        alert.title = title
        alert.headerText = title
        alert.contentText = content
        val answer = alert.showAndWait()
        return answer.get() == ButtonType.OK
    }
}


