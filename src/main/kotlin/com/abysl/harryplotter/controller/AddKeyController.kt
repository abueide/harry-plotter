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

package com.abysl.harryplotter.controller

import com.abysl.harryplotter.data.ChiaKey
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TextField
import javafx.stage.Stage
import java.net.URL
import java.util.ResourceBundle

class AddKeyController : Initializable {
    @FXML
    lateinit var nickname: TextField

    @FXML
    lateinit var fingerprint: TextField

    @FXML
    lateinit var master: TextField

    @FXML
    lateinit var pool: TextField

    @FXML
    lateinit var farmer: TextField

    lateinit var callback: (ChiaKey) -> Unit

    override fun initialize(location: URL?, resources: ResourceBundle?) {
    }

    fun onSave() {
        callback(ChiaKey(nickname.text, fingerprint.text, master.text, pool.text, farmer.text))
        close()
    }

    fun onCancel() {
        close()
    }

    private fun close() {
        val stage = fingerprint.scene.window as Stage
        stage.close()
    }
}
