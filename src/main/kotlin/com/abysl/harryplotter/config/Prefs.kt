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

package com.abysl.harryplotter.config

import com.abysl.harryplotter.util.getResource
import java.util.prefs.Preferences

object Prefs {
    private const val VERSION_PROMPT = "VERSION_PROMPT"
    private const val DARK_MODE = "DARK_MODE"
    private const val EXE_PATH = "EXE_PATH"
    private const val CONFIG_PATH = "CONFIG_PATH"
    private const val STATIC_STAGGER = "STATIC_STAGGER"
    private const val FIRST_PHASE_STAGGER = "FIRST_PHASE_STAGGER"
    private const val OTHER_PHASE_STAGGER = "OTHER_PHASE_STAGGER"

    private val version = "version.txt".getResource().readText()
    private val prefNode = Preferences.userRoot().node("com.abysl.harryplotter")

    var versionPrompt: Boolean
        get() = prefNode.getBoolean(VERSION_PROMPT + version, true)
        set(value) {
            prefNode.putBoolean(VERSION_PROMPT + version, value)
        }

    var darkMode: Boolean
        get() = prefNode.getBoolean(DARK_MODE, false)
        set(value) {
            prefNode.putBoolean(DARK_MODE, value)
        }
    var exePath: String
        get() = prefNode.get(EXE_PATH, "PATH/TO/FILE")
        set(value) {
            prefNode.put(EXE_PATH, value)
        }
    var configPath: String
        get() = prefNode.get(CONFIG_PATH, "PATH/TO/FILE")
        set(value) {
            prefNode.put(CONFIG_PATH, value)
        }
    var staticStagger: Int
        get() = prefNode.getInt(STATIC_STAGGER, 0)
        set(value) {
            prefNode.putInt(STATIC_STAGGER, value)
        }

    var firstStagger: Int
        get() = prefNode.getInt(FIRST_PHASE_STAGGER, 0)
        set(value) {
            prefNode.putInt(FIRST_PHASE_STAGGER, value)
        }

    var otherStagger: Int
        get() = prefNode.getInt(OTHER_PHASE_STAGGER, 0)
        set(value) {
            prefNode.putInt(OTHER_PHASE_STAGGER, value)
        }
}
