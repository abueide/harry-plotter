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

package com.abysl.harryplotter.ui.drives

import com.abysl.harryplotter.model.StaggerSettings
import com.abysl.harryplotter.model.drives.TempDrive
import com.abysl.harryplotter.util.limitToInt
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import java.net.URL
import java.util.ResourceBundle

class TempDriveView : Initializable {
    @FXML
    private lateinit var staticStagger: TextField

    @FXML
    private lateinit var staticIgnore: CheckBox

    @FXML
    private lateinit var maxP1: TextField

    @FXML
    private lateinit var maxOther: TextField

    @FXML
    private lateinit var maxConcurrent: TextField

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        staticStagger.limitToInt()
        maxP1.limitToInt()
        maxOther.limitToInt()
        maxConcurrent.limitToInt()
    }

    fun loadDrive(drive: TempDrive) {
        val stagger = drive.staggerSettings
        staticStagger.text = stagger.staticStagger.toString()
        staticIgnore.isSelected = stagger.staticIgnore
        maxP1.text = stagger.maxFirstStagger.toString()
        maxOther.text = stagger.maxOtherStagger.toString()
        maxConcurrent.text = stagger.maxTotal.toString()
    }

    fun getStagger(): StaggerSettings {
        return StaggerSettings(
            maxP1.text.ifBlank { "0" }.toInt(),
            maxOther.text.ifBlank { "0" }.toInt(),
            maxConcurrent.text.ifBlank { "0" }.toInt(),
            staticStagger.text.ifBlank { "0" }.toInt(),
            staticIgnore.isSelected
        )
    }
}
