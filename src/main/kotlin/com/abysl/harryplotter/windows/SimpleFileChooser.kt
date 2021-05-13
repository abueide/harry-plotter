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

package com.abysl.harryplotter.windows

import javafx.scene.Node
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import java.io.File
import kotlin.system.exitProcess

class SimpleFileChooser(val node: Node, val dialogs: SimpleDialogs = SimpleDialogs()) {

    fun chooseFile(title: String, vararg extensions: FileChooser.ExtensionFilter): File {
        val fileChooser = FileChooser()
        fileChooser.title = title
        fileChooser.extensionFilters.addAll(extensions)
        val file: File? = fileChooser.showOpenDialog(node.scene.window)
        if (file != null) {
            return file
        }
        if (!dialogs.showConfirmation("File Not Selected", "Try again?")) {
            exitProcess(0)
        }
        return chooseFile(title, *extensions)
    }

    fun chooseFile(title: String): File {
        return chooseFile(title, FileChooser.ExtensionFilter("All", "*.*"))
    }

    fun chooseDir(title: String, required: Boolean = false): File? {
        val directoryChooser = DirectoryChooser()
        directoryChooser.title = title
        val directory: File? = directoryChooser.showDialog(node.scene.window)
        if (required) {
            dialogs.showAlert("Directory Not Selected", "Please try again.")
            return chooseDir(title)
        }
        return directory
    }
}
