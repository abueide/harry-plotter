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

package com.abysl.harryplotter.config

import com.abysl.harryplotter.model.drives.Drive
import com.abysl.harryplotter.model.jobs.ChiaKey
import com.abysl.harryplotter.model.jobs.PlotJob
import com.abysl.harryplotter.ui.all.SimpleDialogs
import com.abysl.harryplotter.util.getResource
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object Config {
    val plotterHome = File(System.getProperty("user.home") + "/.harryplotter/")
    val plotLogsRunning = plotterHome.resolve("plotlogs/running")
    val plotLogsFinished = plotterHome.resolve("plotlogs/finished")
    val plotLogsFailed = plotterHome.resolve("plotlogs/failed")
    val version by lazy { "version.txt".getResource()?.readText() ?: throw Exception("Version file not found") }

    private val jobsFile = File(plotterHome.path + "/jobs.json")
    private val drivesFile = File(plotterHome.path + "/drives.json")

    val devkey = ChiaKey(
        nickname = "Developer Donation",
        fingerprint = "3639606261",
        publicKey = "821ac77d286b5a9008f6354d36d600d00cc7be8e5058aac391421a16895522eb7c55e5088d0beabccf82018831caf18e",
        farmerKey = "a24411f3ed5fc2d5a6e3eaefb73aefea30fa7f3b64f065046186ca8191d53fd0c15ca8994ee587ec2a9f85d72e11c7b8",
        poolKey = "xch1hx7e90gm7ag7525t9wlcwc67r2jcvgqym8mu0t9l8rtgckpxrsfs4utw9z"
    )

    init {
        plotterHome.mkdirs()
        plotLogsRunning.mkdirs()
        plotLogsFinished.mkdirs()
        plotLogsFailed.mkdirs()
    }

    fun savePlotJobs(jobs: List<PlotJob>) {
        try {
            jobsFile.writeText(Json.encodeToString(jobs))
        } catch (e: SerializationException) {
            SimpleDialogs.showAlert("Failed to save drives.", "Please report this to the developer.")
            e.printStackTrace()
        }
    }

    fun getPlotJobs(): List<PlotJob> {
        if (!jobsFile.exists()) return emptyList()
        var result: List<PlotJob>?
        try {
            result = Json.decodeFromString(jobsFile.readText())
        } catch (e: Exception) {
            result = null
            SimpleDialogs.showAlert(
                "Failed to decode jobs",
                "Backing them up to ~/.harryplotter/jobs.bak and resetting jobs.json"
            )
            backupFile(jobsFile)
            savePlotJobs(emptyList())
        }
        return result ?: emptyList()
    }

    fun saveDrives(drives: List<Drive>) {
        try {
            drivesFile.writeText(Json.encodeToString(drives))
        } catch (e: SerializationException) {
            SimpleDialogs.showAlert("Failed to save drives.", "Please report this to the developer.")
            e.printStackTrace()
        }
    }

    fun getDrives(): List<Drive> {
        if (!drivesFile.exists()) return emptyList()
        var result: List<Drive>?
        try {
            result = Json.decodeFromString(drivesFile.readText())
        } catch (e: Exception) {
            result = null
            SimpleDialogs.showAlert(
                "Failed to decode drives",
                "Backing them up to ~/.harryplotter/drives.bak and resetting drives.json"
            )
            backupFile(drivesFile)
            saveDrives(emptyList())
        }
        return result ?: emptyList()
    }

    fun backupFile(file: File) {
        val backupFile = File(file.parent + "/${file.nameWithoutExtension}.bak")
        backupFile.delete()
        file.renameTo(backupFile)
    }

    fun resetConfig() {
        plotterHome.deleteRecursively()
    }

//    fun saveTime(desc: JobDescription, result: JobResult) {
//    }

//    fun loadTimes(desc: JobDescription): List<JobResult> {
//        return emptyList()
//    }
}
