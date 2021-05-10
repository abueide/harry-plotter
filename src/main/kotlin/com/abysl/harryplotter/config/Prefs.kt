package com.abysl.harryplotter.config

import java.util.prefs.Preferences


object Prefs {
    private val DARK_MODE = "DARK_MODE"
    private val EXE_PATH = "EXE_PATH"
    private val prefs = Preferences.userRoot().node("com.abysl.harryplotter")

    var darkMode: Boolean
        get() = prefs.getBoolean(DARK_MODE, true)
        set(value){ prefs.putBoolean(DARK_MODE, value) }
    var exePath: String
        get() = prefs.get(EXE_PATH, "PATH/TO/FILE")
        set(value){ prefs.put(EXE_PATH, value) }
}