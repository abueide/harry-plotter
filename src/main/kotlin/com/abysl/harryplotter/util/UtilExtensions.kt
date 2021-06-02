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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.InputStream
import java.net.URL
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

fun String.getResource(): URL {
    return HarryPlotter::class.java.getResource(this)
}

fun String.getResourceAsStream(): InputStream {
    return HarryPlotter::class.java.getResourceAsStream(this)
}

fun String.asFile(): File {
    return File(this)
}

fun List<String>.merge(delimiter: String): String {
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

// Syntax sugar to make chains with multiple StateFlows more readable, example:
// mainViewModel.jobsListViewModel.jobsList.value.first().logs.value ->
// mainViewModel.jobsListViewmodel.jobsList().first().logs()
operator fun <T> StateFlow<T>.invoke(): T {
    return this.value
}

@OptIn(ExperimentalTime::class)
fun Duration.formatted(): String {
    val hours = this.inWholeHours
    val minutes = this.inWholeMinutes - (hours * 60)
    return "${hours}h ${minutes}m (${this.inWholeSeconds}s)"
}

suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
    map { async { f(it) } }.awaitAll()
}

