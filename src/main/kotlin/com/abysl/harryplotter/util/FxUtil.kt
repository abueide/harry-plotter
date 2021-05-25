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

package com.abysl.harryplotter.util

import com.abysl.harryplotter.config.Prefs
import javafx.scene.Scene
import javafx.stage.Stage

object FxUtil {

    fun setTheme(scene: Scene) {
        if (Prefs.darkMode) {
            scene.stylesheets.clear()
            scene.stylesheets.add("themes/Dark.css".getResource().toExternalForm())
        } else {
            scene.stylesheets.clear()
            scene.stylesheets.add("themes/Light.css".getResource().toExternalForm())
        }
    }

    fun setTheme(stage: Stage) {
        setTheme(stage.scene)
    }
}
