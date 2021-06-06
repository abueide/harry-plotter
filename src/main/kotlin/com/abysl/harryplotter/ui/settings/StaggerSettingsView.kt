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

package com.abysl.harryplotter.ui.settings

import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.util.limitToInt
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TextField
import javafx.stage.Stage
import java.net.URL
import java.util.ResourceBundle

class StaggerSettingsView : Initializable {
    @FXML
    private lateinit var firstPhaseStagger: TextField

    @FXML
    private lateinit var otherPhaseStagger: TextField

    @FXML
    private lateinit var staticStagger: TextField

    @FXML
    private lateinit var maxTotal: TextField

    private lateinit var updatedCallback: () -> Unit

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        firstPhaseStagger.limitToInt()
        otherPhaseStagger.limitToInt()
        staticStagger.limitToInt()
        maxTotal.limitToInt()
        firstPhaseStagger.text = Prefs.firstStagger.toString()
        otherPhaseStagger.text = Prefs.otherStagger.toString()
        staticStagger.text = Prefs.staticStagger.toString()
        maxTotal.text = Prefs.maxTotal.toString()
    }

    fun initialized(updatedCallback: () -> Unit) {
        this.updatedCallback = updatedCallback
    }

    fun onCancel() {
        val stage = firstPhaseStagger.scene.window as Stage
        stage.close()
    }

    fun onSave() {
        Prefs.firstStagger = firstPhaseStagger.text.ifBlank { "0" }.toInt()
        Prefs.otherStagger = otherPhaseStagger.text.ifBlank { "0" }.toInt()
        Prefs.staticStagger = staticStagger.text.ifBlank { "0" }.toInt()
        Prefs.maxTotal = maxTotal.text.ifBlank { "0" }.toInt()
        updatedCallback()
        val stage = firstPhaseStagger.scene.window as Stage
        stage.close()
    }
}
