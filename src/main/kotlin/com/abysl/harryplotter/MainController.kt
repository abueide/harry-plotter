package com.abysl.harryplotter

import com.abysl.harryplotter.chia.ChiaCli
import com.abysl.harryplotter.data.Job
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import java.io.File
import java.net.URL
import java.util.*


class MainController : Initializable {
    // UI Components ---------------------------------------------------------------------------------------------------
    @FXML private lateinit var mainBox: VBox
    @FXML private lateinit var jobsBox: VBox
    @FXML private lateinit var statusJobsBox: VBox
    @FXML private lateinit var jobName: TextField
    @FXML private lateinit var tempDir: TextField
    @FXML private lateinit var selectTempDir: Button
    @FXML private lateinit var destDir: TextField
    @FXML private lateinit var threads: TextField
    @FXML private lateinit var ram: TextField
    @FXML private lateinit var chiaKey: ComboBox<*>
    @FXML private lateinit var save: Button
    @FXML private lateinit var stopAll: Button
    @FXML private lateinit var startAll: Button
    @FXML private lateinit var currentStatus: Label
    @FXML private lateinit var lastPlotTime: Label
    @FXML private lateinit var averagePlotTime: Label
    @FXML private lateinit var totalPlotsCreated: Label
    @FXML private lateinit var estimatedPlotsDay: Label
    @FXML private lateinit var averagePlotsDay: Label
    @FXML private lateinit var stop: Button
    @FXML private lateinit var start: Button
    val jobs: ListView<Job> = ListView()

    // Initial State ---------------------------------------------------------------------------------------------------
    val chiaDir = File(System.getProperty("user.home") + "/.chia/mainnet/")
    lateinit var chia: ChiaCli

    override fun initialize(location: URL?, resources: ResourceBundle?) {

    }

    // Calls after the window is initialized so mainBox.scene.window isn't null
    fun initialized(){
        chia = ChiaCli(getExePath())

        // Make the listview in both tabs use the same jobs view so they stay in sync
        jobsBox.children.clear()
        statusJobsBox.children.clear()
        jobsBox.children.add(jobs)
        statusJobsBox.children.add(jobs)
    }

    // User Actions ----------------------------------------------------------------------------------------------------

    fun onStartAll(){

    }

    fun onStart(){
        chia.createPlot(jobs.selectionModel.selectedItem)
    }

    // Utility Functions -----------------------------------------------------------------------------------------------

    fun getExePath(): File {
        var chiaExe = File(System.getProperty("user.home") + "/AppData/Local/chia-blockchain/")

        chiaDir.list().forEach {
            if (it.contains("app-")) {
                chiaExe = File(chiaExe.path + "/$it/resources/app.asar.unpacked/daemon/chia.exe")
                return chiaExe
            }
        }
        showAlert("Chia Executable Not Found", "Please specify the chia executable location")
        val file = chooseFile(
            "Select Chia Executable",
            FileChooser.ExtensionFilter("All Files", "*.*",),
            FileChooser.ExtensionFilter("Executable File", ".exe",)
            )

        if(file.name.startsWith("chia"))
            return file
        else {
            showAlert("Wrong Executable", "Looking for the chia cli executable (chia.exe lowercase)")
            return getExePath()
        }
    }

    fun chooseFile(title: String): File{
        return chooseFile(title, FileChooser.ExtensionFilter("All", "*.*"))
    }

    fun chooseFile(title: String, vararg extensions: FileChooser.ExtensionFilter): File {
        val fileChooser = FileChooser()
        fileChooser.title = title
        fileChooser.extensionFilters.addAll(extensions)
        val file: File? = fileChooser.showOpenDialog(mainBox.scene.window)
        if(file != null){
            return file
        }
        showAlert("File Not Selected", "Please try again.")
        return chooseFile(title, *extensions)
    }

    fun chooseDir(title: String): File {
        val directoryChooser = DirectoryChooser()
        directoryChooser.title = title
        val directory: File? = directoryChooser.showDialog(mainBox.scene.window)
        if(directory != null)
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


}
