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

import java.util.prefs.Preferences


object Prefs {
    private val DARK_MODE = "DARK_MODE"
    private val EXE_PATH = "EXE_PATH"
    private val prefs = Preferences.userRoot().node("com.abysl.harryplotter")

    var darkMode: Boolean
        get() = prefs.getBoolean(DARK_MODE, false)
        set(value){ prefs.putBoolean(DARK_MODE, value) }
    var exePath: String
        get() = prefs.get(EXE_PATH, "PATH/TO/FILE")
        set(value){ prefs.put(EXE_PATH, value) }
}