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

package com.abysl.harryplotter.chia

import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.windows.SimpleDialogs
import com.abysl.harryplotter.windows.SimpleFileChooser
import javafx.scene.Node
import javafx.stage.FileChooser
import java.io.File
import kotlin.system.exitProcess

class ChiaLocator(node: Node) {
    private val dialogs = SimpleDialogs()
    private val fileChooser = SimpleFileChooser(node, dialogs)

    fun getConfigFile(): File {
        val configDir = File(System.getProperty("user.home") + "/.chia/mainnet/config/config.yaml")
        if (configDir.exists()) {
            return configDir
        }
        dialogs.showAlert(
            "Chia Config File Not Found",
            "Please specify the chia config location, usually located at C:\\Users\\YourUser\\.chia\\mainnet\\config\\config.yaml"
        )
        val file = fileChooser.chooseFile(
            "Select Chia Config File",
            FileChooser.ExtensionFilter("YAML Config File", "config.yaml")
        )

        if (file.name.equals("config.yaml") && file.exists())
            return file
        else {
            if (dialogs.showConfirmation(
                    "Wrong File",
                    "Looking for config.yaml, usually located at C:\\Users\\YourUser\\.chia\\mainnet\\config\\config.yaml . Try again?"
                )
            ) {
                return getConfigFile()
            } else {
                exitProcess(0)
            }
        }
    }

    fun getExePath(): File {
        val lastPath = File(Prefs.exePath)
        if (lastPath.exists()) return lastPath
        val macChiaExe = File("/Applications/Chia.app/Contents/Resources/app.asar.unpacked/daemon/chia")
        if (macChiaExe.exists()) return macChiaExe

        var chiaAppData = File(System.getProperty("user.home") + "/AppData/Local/chia-blockchain/")

        if (chiaAppData.exists()) {
            chiaAppData.list()?.forEach {
                if (it.contains("app-")) {
                    chiaAppData = File(chiaAppData.path + "/$it/resources/app.asar.unpacked/daemon/chia.exe")
                    return chiaAppData
                }
            }
        }
        dialogs.showAlert("Chia Executable Not Found", "Please specify the chia executable location")
        val file = fileChooser.chooseFile(
            "Select Chia Executable",
            FileChooser.ExtensionFilter("All Files", "*.*"),
            FileChooser.ExtensionFilter("Executable File", "*.exe")
        )

        if (file.name.startsWith("chia"))
            return file
        else {
            if (dialogs.showConfirmation(
                    "Wrong File",
                    "Looking for the chia cli executable (chia.exe lowercase). Try again?"
                )
            ) {
                return getExePath()
            } else {
                exitProcess(0)
            }
        }
    }
}
