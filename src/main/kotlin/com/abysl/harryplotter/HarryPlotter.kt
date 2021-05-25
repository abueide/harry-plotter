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

package com.abysl.harryplotter

import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.util.FxUtil
import com.abysl.harryplotter.util.getResource
import com.abysl.harryplotter.util.getResourceAsStream
import com.abysl.harryplotter.view.MainView
import com.abysl.harryplotter.windows.VersionPromptWindow
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.stage.Stage

class HarryPlotter : Application() {
    lateinit var mainStage: Stage
    override fun start(stage: Stage) {
        mainStage = stage
        val loader = FXMLLoader("fxml/MainView.fxml".getResource())
        val root: Parent = loader.load()
        val view: MainView = loader.getController()
        val scene = Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT)
        view.initialized()

        stage.icons.add(Image("icons/snitch.png".getResourceAsStream()))
        stage.scene = scene
        FxUtil.setTheme(stage)
        stage.show()
        view.toggleTheme = ::toggleTheme
        view.hostServices = hostServices
        stage.setOnCloseRequest {
            view.onExit()
        }

        if (Prefs.versionPrompt) {
            VersionPromptWindow.show()
        }
    }

    fun toggleTheme() {
        Prefs.darkMode = !Prefs.darkMode
        FxUtil.setTheme(mainStage)
    }

    companion object {
        const val DEFAULT_WIDTH = 1080.0
        const val DEFAULT_HEIGHT = 720.0
    }
}

fun main() {
    Application.launch(HarryPlotter::class.java)
}
