package com.abysl.harryplotter.config

import java.util.prefs.Preferences

object Prefs {
    private val DARK_MODE = "DARK_MODE"
    private val prefs = Preferences.userRoot().node("com.abysl.harryplotter")

    var darkMode: Boolean
        get() = prefs.getBoolean(DARK_MODE, true)
        set(value){
            prefs.putBoolean(DARK_MODE, value)
        }
}