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

package com.abysl.harryplotter.util

import com.abysl.harryplotter.HarryPlotter
import javafx.collections.ObservableList
import javafx.scene.control.TextField
import java.io.InputStream
import java.net.URL

fun String.getResource(): URL {
    return HarryPlotter::class.java.getResource(this)
}

fun String.getResourceAsStream(): InputStream {
    return HarryPlotter::class.java.getResourceAsStream(this)
}

fun List<String>.merge(delimiter: String): String{
    val builder = StringBuilder()
    this.forEach {
        builder.append("$it$delimiter")
    }
    return builder.toString()
}

fun List<String>.unlines(): String {
    return merge("\n")
}

fun List<String>.unwords(): String {
    return merge(" ")
}

fun TextField.limitToInt() {
    textProperty().addListener { observable, oldValue, newValue ->
        if (!newValue.matches(Regex("\\d*"))) {
            text = newValue.replace("[^\\d]".toRegex(), "")
        }
    }
}