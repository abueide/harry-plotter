package com.abysl.harryplotter.util

import com.abysl.harryplotter.HarryPlotter
import java.lang.StringBuilder
import java.net.URL

fun String.getResource(): URL {
    return HarryPlotter::class.java.getResource(this)
}

fun List<out String>.unlines(): String {
    val builder = StringBuilder()
    this.forEach {
        builder.append("$it ")
    }
    return builder.toString()
}