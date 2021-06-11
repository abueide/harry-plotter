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

package com.abysl.harryplotter.ui.main

import com.abysl.harryplotter.config.Config
import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.util.fx.FxUtil
import com.abysl.harryplotter.util.getResource
import javafx.scene.Scene
import javafx.scene.control.TextArea
import javafx.scene.text.Font
import javafx.stage.Modality
import javafx.stage.Stage
import java.io.IOException
import kotlin.system.exitProcess

object VersionPromptWindow {
    private const val WIDTH = 800
    private const val HEIGHT = 600
    private const val FONT_SIZE = 14.0
    fun show() {
        try {
            // Load scene
            val stage = Stage()
            val textPrompt = TextArea()
            textPrompt.font = Font.font(FONT_SIZE)
            textPrompt.text = "changelogs/${Config.version}.txt".getResource().readText()
            textPrompt.wrapTextProperty().set(true)
            textPrompt.editableProperty().set(false)
            stage.width = WIDTH.toDouble()
            stage.height = HEIGHT.toDouble()
            stage.scene = Scene(textPrompt)
            stage.title = "Version ${Config.version}"
            stage.initModality(Modality.APPLICATION_MODAL)
            FxUtil.setTheme(stage)
            stage.show()
            Prefs.versionPrompt = false
        } catch (ex: IOException) {
            System.err.println(ex)
            exitProcess(1)
        }
    }
}

