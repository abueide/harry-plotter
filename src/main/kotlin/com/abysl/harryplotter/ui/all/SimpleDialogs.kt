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

import com.abysl.harryplotter.util.fx.FxUtil
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.ChoiceDialog

object SimpleDialogs {

    fun showOptions(title: String, vararg options: String, callback: (String) -> Unit) {
        val choice = ChoiceDialog(options.first(), options.toList())
        choice.title = title
        choice.show()
        choice.resultProperty().addListener { observable, oldValue, newValue ->
            callback(newValue)
        }
    }

    fun showOptionsBlocking(title: String, vararg options: String): String? {
        val choice = ChoiceDialog(options.first(), options.toList())
        choice.title = title
        val result = choice.showAndWait()
        return if (result.isPresent) result.get() else null
    }

    fun showConfirmation(title: String, content: String): Boolean {
        val alert = Alert(Alert.AlertType.CONFIRMATION)
        alert.title = title
        alert.headerText = title
        alert.contentText = content
        FxUtil.setTheme(alert.dialogPane.scene)
        val answer = alert.showAndWait()
        return answer.get() == ButtonType.OK
    }

    fun showAlert(title: String, content: String) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = title
        alert.headerText = title
        alert.contentText = content
        FxUtil.setTheme(alert.dialogPane.scene)
        alert.showAndWait()
    }
}
