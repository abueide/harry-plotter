package com.abysl.harryplotter.controller

import com.abysl.harryplotter.HarryPlotter
import com.abysl.harryplotter.data.ChiaKey
import javafx.collections.ObservableList
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Stage
import java.io.IOException
import kotlin.system.exitProcess

class AddKey(val keys: ObservableList<ChiaKey>) {

    fun update(key: ChiaKey){
        keys.add(key)
    }

    fun show() {
        try {
            //Load second scene
            val loader = FXMLLoader(HarryPlotter::class.java.getResource("fxml/addkey.fxml"))
            val root = loader.load<Parent>()

            //Get controller of scene2
            val controller: AddKeyController = loader.getController()
            controller.callback = ::update
            //Pass whatever data you want. You can have multiple method calls here
            //Show scene 2 in new window
            val stage = Stage()
            stage.scene = Scene(root)
            stage.title = "Add Key"
            stage.isAlwaysOnTop = true;
            stage.initModality(Modality.APPLICATION_MODAL)
            stage.show()
        } catch (ex: IOException) {
            System.err.println(ex)
            exitProcess(1)
        }
    }
}