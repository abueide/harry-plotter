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

import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.windows.SimpleFileChooser
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TextField
import javafx.stage.Stage
import java.net.URL
import java.util.*

class ChiaSettingsView: Initializable{
    @FXML
    private lateinit var exePath: TextField
    @FXML
    private lateinit var configPath: TextField

    private lateinit var fileChooser: SimpleFileChooser


    override fun initialize(location: URL?, resources: ResourceBundle?) {
        fileChooser = SimpleFileChooser(exePath)
        exePath.text = Prefs.exePath
        configPath.text = Prefs.configPath
    }

    fun onExeBrowse(){
        fileChooser.chooseFileMaybe("Select chia executable")?.let {
            exePath.text = it.path
        }
    }

    fun onConfigBrowse(){
        fileChooser.chooseFileMaybe("Select chia config.yaml")?.let {
            exePath.text = it.path
        }
    }

    fun onCancel(){
        val stage = exePath.scene.window as Stage
        stage.close()
    }

    fun onSave(){
        Prefs.exePath = exePath.text
        val stage = exePath.scene.window as Stage
        stage.close()
    }

}