package com.abysl.harryplotter.util

import com.abysl.harryplotter.HarryPlotter
import java.net.URL

fun String.getResource(): URL {
    return HarryPlotter::class.java.getResource(this)
}