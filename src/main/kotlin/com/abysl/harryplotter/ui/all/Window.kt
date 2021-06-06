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

package com.abysl.harryplotter.ui.all

import com.abysl.harryplotter.HarryPlotter
import com.abysl.harryplotter.util.fx.FxUtil
import com.abysl.harryplotter.util.getResource
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Stage
import java.io.IOException
import kotlin.system.exitProcess

abstract class Window<T>(val width: Int = 800, val height: Int = 600) {

    protected val stage: Stage = Stage()
    protected fun create(title: String, fxml: String): T {
        try {
            // Load scene
            val loader = FXMLLoader(fxml.getResource())
            val root = loader.load<Parent>()
            // Get controller from scene
            val controller: T = loader.getController()
//            stage.isAlwaysOnTop = true
            stage.initModality(Modality.APPLICATION_MODAL)
            stage.scene = Scene(root)
            stage.title = title
            stage.width = width.toDouble()
            stage.height = height.toDouble()
            FxUtil.setTheme(stage)
            stage.show()
            return controller
        } catch (ex: IOException) {
            System.err.println(ex)
            ex.printStackTrace()
            exitProcess(1)
        }
    }
}
