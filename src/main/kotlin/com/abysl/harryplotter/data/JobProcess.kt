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

package com.abysl.harryplotter.data

import com.abysl.harryplotter.chia.ChiaCli
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File

class JobProcess(val chia: ChiaCli, val jobDesc: JobDescription) {
    var proc: Process? = null
    val logs: ObservableList<String> = FXCollections.observableArrayList()

    var state: JobState = JobState()
    var stats: JobStats = JobStats()

    fun start() {
        if (state.running || proc?.isAlive == true) {
            println("Trying to start new process while old one is still running, ignoring start job.")
        } else {
            state.status = RUNNING
            state.running = true

            proc = chia.runCommandAsync(
                ioDelay = 10,
                outputCallback = ::parseLine,
                completedCallback = ::whenDone,
                "plots",
                "create",
                "-k", "32",
                "-a", jobDesc.key.fingerprint,
                "-b", jobDesc.ram.toString(),
                "-r", jobDesc.threads.toString(),
                "-t", jobDesc.tempDir.toString(),
                "-d", jobDesc.destDir.toString(),
            )
        }
    }

    fun stop(block: Boolean = false) {
        // Store in immutable variable so it doesn't try to delete files after state is wiped out
        val id: String = state.plotId
        logs.clear()
        state = JobState()
        proc?.destroyForcibly()
        deleteTempFiles(id, block)
    }

    private fun deleteTempFiles(plotId: String, block: Boolean) {
        if (plotId.isNotBlank()) {
            val files = jobDesc.tempDir.listFiles()
                ?.filter { it.toString().contains(plotId) && it.extension == "tmp" }
                ?.map { deleteFile(it) }
            if (block) {
                runBlocking {
                    files?.forEach { it.await() }
                }
            }
        }
    }

    private fun deleteFile(file: File, delayTime: Long = 100, maxTries: Int = 100) =
        CoroutineScope(Dispatchers.IO).async {
            var timeout = 0
            while (file.exists() && !file.delete() && timeout++ < maxTries) {
                println("Couldn't delete file, trying again in $delayTime ms")
                delay(delayTime)
            }
            if (timeout < maxTries) {
                println("Deleted: " + file.name)
                return@async true
            } else {
                return@async false
            }
        }

    private fun whenDone() {
        stats.plotsDone++
        stats.results.add(state.currentResult)
        if (state.running && (stats.plotsDone < jobDesc.plotsToFinish || jobDesc.plotsToFinish == 0)) {
            stop()
            start()
        } else {
            stop()
        }
    }

    fun parseLine(line: String) {
        Platform.runLater {
            logs.add(line.trim())
        }
        try {
            if (line.contains("ID: ")) {
                state.plotId = line.split("ID: ").last()
            }

            when {
                line.contains("Starting phase") -> {
                    state.phase = line
                        .split("Starting phase ").last()
                        .split("/").first()
                        .toInt()
                    println(state.phase)
                }
                line.contains("tables") -> {
                    state.subphase = line.split("tables ").last()
                    println(state.subphase)
                }
                line.contains("table") -> {
                    val split = line.split("table ")
                    state.subphase = line.split("table ").last()
                    println(state.subphase)
                }
                line.contains("Time for phase") -> {
                    val phase: Int = line.split("phase ")[1].split(" =").first().toInt()
                    val seconds: Double = line.split("= ")[1].split(" seconds").first().toDouble()
                    when (phase) {
                        1 -> state.currentResult = state.currentResult.merge(JobResult(phaseOneTime = seconds))
                        2 -> state.currentResult = state.currentResult.merge(JobResult(phaseTwoTime = seconds))
                        3 -> state.currentResult = state.currentResult.merge(JobResult(phaseThreeTime = seconds))
                        4 -> state.currentResult = state.currentResult.merge(JobResult(phaseFourTime = seconds))
                    }
                    println(state.currentResult)
                }
                line.contains("Total time") -> {
                    val seconds: Double = line.split("= ")[1].split(" seconds").first().toDouble()
                    state.currentResult = state.currentResult.merge(JobResult(totalTime = seconds))
                    println(state.currentResult)
                }
                line.contains("Copy time") -> {
                    val seconds: Double = line.split("= ")[1].split(" seconds").first().toDouble()
                    state.currentResult = state.currentResult.merge(JobResult(copyTime = seconds))
                    println(state.currentResult)
                }
            }
        } catch (e: NoSuchElementException) {
            println("WARNING: Fix Line Parser")
            println(e)
        }
    }

    override fun toString(): String {
        return if (state.running) "$jobDesc - ${state.percentage}%" else jobDesc.toString()
    }

    companion object {
        const val STOPPED = "Stopped"
        const val RUNNING = "Running"
        const val ERROR = "Error"
    }
}
