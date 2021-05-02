package com.abysl.harryplotter.util

import com.abysl.harryplotter.config.Prefs
import javafx.stage.Stage
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

object FxUtil {
    fun setTheme(stage: Stage){
        if (Prefs.darkMode){
            stage.scene.stylesheets.clear()
            stage.scene.stylesheets.add("themes/Dark.css".getResource().toExternalForm())
        }else {
            stage.scene.stylesheets.clear()
            stage.scene.stylesheets.add("themes/Light.css".getResource().toExternalForm())
        }
    }
}