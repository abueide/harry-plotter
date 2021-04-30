package com.abysl.harryplotter.controller

import com.abysl.harryplotter.HarryPlotter
import com.abysl.harryplotter.data.ChiaKey
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.TextField
import javafx.stage.Modality
import javafx.stage.Stage
import java.io.IOException
import java.net.URL
import java.util.*
import kotlin.system.exitProcess


class AddKeyController: Initializable {
    @FXML
    lateinit var nickname: TextField

    @FXML
    lateinit var fingerprint: TextField


    @FXML
    lateinit var master: TextField

    @FXML
    lateinit var pool: TextField

    @FXML
    lateinit var farmer: TextField

    lateinit var callback: (ChiaKey) -> Unit

    override fun initialize(location: URL?, resources: ResourceBundle?) {

    }

    fun onSave(){
        callback(ChiaKey(nickname.text, fingerprint.text, master.text, pool.text, farmer.text))
        close()
    }

    fun onCancel(){
        close()
    }

    private fun close(){
        val stage = fingerprint.scene.window as Stage
        stage.close()
    }

}
