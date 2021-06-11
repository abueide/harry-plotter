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

package com.abysl.harryplotter.ui.jobs

import com.abysl.harryplotter.model.jobs.ChiaKey
import javafx.fxml.FXML
import javafx.scene.control.TextField
import javafx.stage.Stage

class KeyEditorView {
    @FXML
    lateinit var nickname: TextField

    @FXML
    lateinit var fingerprint: TextField

    @FXML
    lateinit var publicKey: TextField

    @FXML
    lateinit var farmerKey: TextField

    @FXML
    lateinit var poolKey: TextField

    lateinit var callback: (ChiaKey?) -> Unit

    fun onSave() {
        callback(readKey())
        close()
    }

    fun onCancel() {
        close()
    }

    private fun close() {
        val stage = fingerprint.scene.window as Stage
        stage.close()
    }

    private fun readKey(): ChiaKey? {
        return if (fingerprint.text.isBlank() && (poolKey.text.isBlank() || farmerKey.text.isBlank())) {
            null
        } else ChiaKey(
            nickname.text.trim(),
            fingerprint.text.trim(),
            publicKey.text.trim(),
            farmerKey.text.trim(),
            poolKey.text.trim()
        )
    }

    fun writeKey(key: ChiaKey?) {
        key?.let {
            nickname.text = it.nickname
            fingerprint.text = it.fingerprint
            publicKey.text = it.publicKey
            farmerKey.text = it.farmerKey
            poolKey.text = it.poolKey
        }
    }
}
