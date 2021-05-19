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

package com.abysl.harryplotter.model

import com.abysl.harryplotter.chia.ChiaCli
import com.abysl.harryplotter.model.records.JobDescription
import com.abysl.harryplotter.util.invoke
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.File

@Serializable
class PlotJob(var description: JobDescription, val stats: JobStats = JobStats()) {
    @Transient
    val state: JobState = JobState()

    @Transient
    lateinit var chia: ChiaCli

    fun init(chia: ChiaCli){
        this.chia = chia
    }

     fun start() {
        if (state.running() || state.proc()?.isAlive == true) {
            println("Trying to start new process while old one is still running, ignoring start job.")
        } else {
            state.running(true)

            val args = mutableListOf<String>()
            args.addAll(listOf("plots", "create", "-k", "32"))
            if (description.key.fingerprint.isNotBlank()) args.addAll(listOf("-a", description.key.fingerprint))
            else if (description.key.farmerKey.isNotBlank() && description.key.poolKey.isNotBlank()) {
                args.addAll(listOf("-f", description.key.farmerKey, "-p", description.key.poolKey))
            }
            if (description.ram > MINIMUM_RAM) args.addAll(listOf("-b", description.ram.toString()))
            if (description.threads > 0) args.addAll(listOf("-r", description.threads.toString()))
            state.proc(chia.runCommandAsync(
                ioDelay = 10,
                outputCallback = ::parseLine,
                completedCallback = ::whenDone,
                "plots",
                "create",
                "-k", "32",
                "-a", description.key.fingerprint,
                "-b", description.ram.toString(),
                "-r", description.threads.toString(),
                "-t", description.tempDir.toString(),
                "-d", description.destDir.toString(),
            ))
        }
    }

    // block boolean used so that we can finish deleting temp files before the program exits. Otherwise, we don't want
    // to block the main thread while deleting files.
    fun stop(block: Boolean = false) {
        // Store in immutable variable so it doesn't try to delete files after state is wiped out
        deleteTempFiles(state.plotId(), block)
        state.proc()?.destroyForcibly()
        state.reset()
    }

    private fun deleteTempFiles(plotId: String, block: Boolean) {
        if (plotId.isNotBlank()) {
            val files = description.tempDir.listFiles()
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
                if (timeout > 1) {
                    println("Couldn't delete file, trying again in $delayTime ms. ${file.name}")
                }
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
        stats.plotsDone.value++
        stats.results.value += state.currentResult()
        if (state.running() && (stats.plotsDone() < description.plotsToFinish || description.plotsToFinish == 0)) {
            stop()
            start()
        } else {
//            stop()
        }
    }

    fun parseLine(line: String) {
        state.logs.value += line.trim()
        try {
            if (line.contains("ID: ")) {
                state.plotId(line.split("ID: ").last())
            }

            when {
                line.contains("Starting phase") -> {
                    val phase = line
                        .split("Starting phase ").last()
                        .split("/").first()
                        .toInt()
                    state.phase(phase)
                    println(state.phase)
                }
                line.contains("tables") -> {
                    val subphase = line.split("tables ").last()
                    state.subphase(subphase)
                    println(state.subphase)
                }
                line.contains("table") -> {
                    state.subphase(line.split("table ").last())
                    println(state.subphase)
                }
                line.contains("Time for phase") -> {
                    val phase: Int = line.split("phase ")[1].split(" =").first().toInt()
                    val seconds: Double = line.split("= ")[1].split(" seconds").first().toDouble()
                    when (phase) {
                        1 -> state.currentResult.value += JobResult(phaseOneTime = seconds)
                        2 -> state.currentResult.value += JobResult(phaseTwoTime = seconds)
                        3 -> state.currentResult.value += JobResult(phaseThreeTime = seconds)
                        4 -> state.currentResult.value += JobResult(phaseFourTime = seconds)
                    }
                    println(state.currentResult)
                }
                line.contains("Total time") -> {
                    val seconds: Double = line.split("= ")[1].split(" seconds").first().toDouble()
                    state.currentResult.value += JobResult(totalTime = seconds)
                    println(state.currentResult)
                }
                line.contains("Copy time") -> {
                    val seconds: Double = line.split("= ")[1].split(" seconds").first().toDouble()
                    state.currentResult.value += JobResult(copyTime = seconds)
                    println(state.currentResult)
                }
            }
        } catch (e: NoSuchElementException) {
            println("WARNING: Fix Line Parser")
            println(e)
        }
    }

    override fun toString(): String {
        return if (state.running()) "$description - ${state.percentage}%" else description.toString()
    }

    companion object {
        private const val MINIMUM_RAM = 2500 // MiB
    }
}
