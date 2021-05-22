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

package com.abysl.harryplotter.windows

import com.abysl.harryplotter.HarryPlotter
import com.abysl.harryplotter.model.records.ChiaKey
import com.abysl.harryplotter.util.FxUtil
import com.abysl.harryplotter.view.ChiaSettingsView
import com.abysl.harryplotter.view.KeyEditorView
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Stage
import java.io.IOException
import kotlin.system.exitProcess

class ChiaSettingsWindow() {

    fun show() {
        try {
            // Load scene
            val loader = FXMLLoader(HarryPlotter::class.java.getResource("fxml/ChiaSettings.fxml"))
            val root = loader.load<Parent>()
            val stage = Stage()
            stage.scene = Scene(root)
            stage.title = "Chia Settings"
            stage.isAlwaysOnTop = true
            stage.initModality(Modality.APPLICATION_MODAL)
            FxUtil.setTheme(stage)
            stage.show()
        } catch (ex: IOException) {
            println(ex)
            ex.printStackTrace()
            exitProcess(1)
        }
    }
}
