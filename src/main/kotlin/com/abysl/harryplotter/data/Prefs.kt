package com.abysl.harryplotter.data

import java.util.prefs.Preferences

object Prefs {
    private val DARK_MODE = "DARK_MODE"
    private val prefs = Preferences.userRoot().node("com.abys.harryplotter")

    var darkMode: Boolean
        get() = prefs.getBoolean(DARK_MODE, true)
        set(value){
            prefs.putBoolean(DARK_MODE, value)
        }
}