package com.abysl.harryplotter.util

import com.abysl.harryplotter.HarryPlotter
import java.io.InputStream
import java.lang.StringBuilder
import java.net.URL

fun String.getResource(): URL {
    return HarryPlotter::class.java.getResource(this)
}

fun String.getResourceAsStream(): InputStream{
    return HarryPlotter::class.java.getResourceAsStream(this)
}

fun List<String>.unlines(): String {
    val builder = StringBuilder()
    this.forEach {
        builder.append("$it ")
    }
    return builder.toString()
}