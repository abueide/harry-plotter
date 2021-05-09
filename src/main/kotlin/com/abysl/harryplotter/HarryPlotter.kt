package com.abysl.harryplotter

import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.controller.MainController
import com.abysl.harryplotter.util.FxUtil
import com.abysl.harryplotter.util.getResource
import com.abysl.harryplotter.util.getResourceAsStream
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage


class HarryPlotter: Application() {
    lateinit var mainStage: Stage
    override fun start(stage: Stage) {
        mainStage = stage
        val loader = FXMLLoader("fxml/main.fxml".getResource())
        val root: Parent = loader.load();
        val controller: MainController = loader.getController()
        val scene = Scene(root, 1080.0, 720.0)

        stage.icons.add(Image("icons/snitch.png".getResourceAsStream()))
        stage.scene = scene
        FxUtil.setTheme(stage)
        stage.show()
        controller.toggleTheme = ::toggleTheme
        controller.initialized()
        stage.setOnCloseRequest {
            controller.onExit()
        }
    }

    fun toggleTheme(){
        Prefs.darkMode = !Prefs.darkMode
        FxUtil.setTheme(mainStage)
    }
}
