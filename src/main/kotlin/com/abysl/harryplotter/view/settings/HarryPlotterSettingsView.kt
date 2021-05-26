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

package com.abysl.harryplotter.view.settings

import com.abysl.harryplotter.config.Config
import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.windows.SimpleDialogs
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.stage.Stage

class HarryPlotterSettingsView {

    @FXML
    lateinit var close: Button

    fun onReset() {
        if (confirm()) {
            Prefs.resetPrefs(close)
            Config.resetConfig()
        }
    }

    fun onClose() {
        (close.scene.window as Stage).close()
    }

    fun confirm(): Boolean {
        return SimpleDialogs.showConfirmation(
            "Are you sure?",
            "This will reset all settings and delete any existing jobs and stats."
        )
    }
}
