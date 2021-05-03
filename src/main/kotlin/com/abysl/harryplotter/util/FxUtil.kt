package com.abysl.harryplotter.util

import com.abysl.harryplotter.config.Prefs
import javafx.scene.Scene
import javafx.stage.Stage
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

object FxUtil {


    fun setTheme(scene: Scene){
        if (Prefs.darkMode){
            scene.stylesheets.clear()
            scene.stylesheets.add("themes/Dark.css".getResource().toExternalForm())
        }else {
            scene.stylesheets.clear()
            scene.stylesheets.add("themes/Light.css".getResource().toExternalForm())
        }
    }

    fun setTheme(stage: Stage){
        setTheme(stage.scene)
    }
}