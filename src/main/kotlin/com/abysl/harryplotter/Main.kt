package com.abysl.harryplotter

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.stage.Stage


class Main: Application() {
    override fun start(stage: Stage) {
        val loader = FXMLLoader(javaClass.getResource("fxml/main.fxml"))
        val root: Parent = loader.load();
        val controller: MainController = loader.getController()
        val scene = Scene(root, 1080.0, 720.0)
        stage.setScene(scene)
        stage.show()

        controller.initialized()
    }

    fun main(){
        launch()
    }
}