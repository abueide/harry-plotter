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
import com.abysl.harryplotter.windows.SimpleDialogs.showAlert
import com.abysl.harryplotter.windows.SimpleDialogs.showConfirmation
import com.abysl.harryplotter.windows.SimpleFileChooser
import javafx.scene.Node
import javafx.stage.FileChooser
import java.io.File
import kotlin.system.exitProcess

class ChiaLocator(node: Node) {

    private val fileChooser = SimpleFileChooser(node)

    fun getConfigFile(): File {
        val prefsDir = File(Prefs.configPath)
        if (prefsDir.exists()) {
            return prefsDir
        }
        val configDir = File(System.getProperty("user.home") + "/.chia/mainnet/config/config.yaml")
        if (configDir.exists()) {
            return configDir
        }
        showAlert(CONFIG_NOT_FOUND, CONFIG_NOT_FOUND_INSTRUCTION)
        val file = fileChooser.chooseFile(
            "Select Chia Config File",
            FileChooser.ExtensionFilter("YAML Config File", "config.yaml")
        )
        if (file.name.equals("config.yaml") && file.exists()) return file
        if (showConfirmation(WRONG_FILE, WRONG_CONFIG)) {
            return getConfigFile()
        } else {
            exitProcess(0)
        }
    }

    fun getExePath(): File {
        val lastPath = File(Prefs.exePath)
        if (checkExe(lastPath)) return lastPath
        val macChiaExe = File(MAC_CHIA_PATH)
        if (checkExe(macChiaExe)) return macChiaExe
        val linuxChiaExe = File(LINUX_CHIA_PATH)
        if (checkExe(linuxChiaExe)) return linuxChiaExe

        val chiaAppData = File(System.getProperty("user.home") + "/AppData/Local/chia-blockchain/")

        if (chiaAppData.exists()) {
            chiaAppData.list()
                ?.lastOrNull { it.contains("app-") }
                ?.let { return File(chiaAppData.path + "/$it/resources/app.asar.unpacked/daemon/chia.exe") }
        }
        showAlert("Chia Executable Not Found", "Please specify the chia executable location")
        val file = fileChooser.chooseFile(
            "Select Chia Executable",
            FileChooser.ExtensionFilter("All Files", "*"),
        )
        if (checkExe(file)) return file
        if (showConfirmation("Wrong File", EXE_NOT_FOUND)) return getExePath() else exitProcess(0)
    }

    fun checkExe(file: File): Boolean {
        return file.exists() && (file.name.equals("chia") || file.name.equals("chia.exe"))
    }

    companion object {
        private const val MAC_CHIA_PATH = "/Applications/Chia.app/Contents/Resources/app.asar.unpacked/daemon/chia"
        private const val LINUX_CHIA_PATH = "/usr/lib/chia-blockchain/resources/app.asar.unpacked/daemon/chia"
        private const val WRONG_CONFIG = "Looking for config.yaml, usually located at " +
            "C:\\Users\\YourUser\\.chia\\mainnet\\config\\config.yaml. " +
            "Try again?"
        private const val WRONG_FILE = "Wrong File"
        private const val CONFIG_NOT_FOUND = "Chia Config File Not Found"
        private const val CONFIG_NOT_FOUND_INSTRUCTION =
            "Please specify the chia config location, usually located at " +
                "C:\\Users\\YourUser\\.chia\\mainnet\\config\\config.yaml"
        private const val EXE_NOT_FOUND =
            "Looking for the chia cli executable (\"chia.exe\" or \"chia\" lowercase). Try again?"
    }
}
