package com.abysl.harryplotter

import com.abysl.harryplotter.data.Prefs
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.stage.Stage


class HarryPlotter: Application() {
    lateinit var mainStage: Stage
    override fun start(stage: Stage) {
        mainStage = stage
        val loader = FXMLLoader(javaClass.getResource("fxml/main.fxml"))
        val root: Parent = loader.load();
        val controller: MainController = loader.getController()
        val scene = Scene(root, 1080.0, 720.0)

        stage.scene = scene
        if(Prefs.darkMode) {
            mainStage.scene.stylesheets.add(javaClass.getResource("themes/Dark.css").toExternalForm())
        }
        stage.show()
        controller.toggleTheme = ::toggleTheme
        controller.initialized()
        stage.setOnCloseRequest {
            controller.onExit()
        }
    }

    fun toggleTheme(){
        Prefs.darkMode = !Prefs.darkMode
        if (Prefs.darkMode){
            mainStage.scene.stylesheets.clear()
            mainStage.scene.stylesheets.add(javaClass.getResource("themes/Dark.css").toExternalForm())
        }else {
            mainStage.scene.stylesheets.clear()
            mainStage.scene.stylesheets.add(javaClass.getResource("themes/Light.css").toExternalForm())
        }
    }

    fun main(){
        launch()
    }
}