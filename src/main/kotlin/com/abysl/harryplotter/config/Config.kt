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

import com.abysl.harryplotter.model.PlotJob
import com.abysl.harryplotter.model.records.ChiaKey
import com.abysl.harryplotter.windows.SimpleDialogs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.lang.Exception

object Config {
    val plotterHome = File(System.getProperty("user.home") + "/.harryplotter/")
    val plotLogsRunning = plotterHome.resolve("plotlogs/running")
    val plotLogsFinished = plotterHome.resolve("plotlogs/finished")

    private val jobsFile = File(plotterHome.path + "/jobs.json")

    val devkey = ChiaKey(
        nickname = "Developer Donation",
        fingerprint = "3639606261",
        publicKey = "821ac77d286b5a9008f6354d36d600d00cc7be8e5058aac391421a16895522eb7c55e5088d0beabccf82018831caf18e",
        farmerKey = "a24411f3ed5fc2d5a6e3eaefb73aefea30fa7f3b64f065046186ca8191d53fd0c15ca8994ee587ec2a9f85d72e11c7b8",
        poolKey = "967bb4d1bfc97c1960cdefa6b41ed8751fee9cba7b2ee8e8a02f922289edece5337d14c426d953e3bdcb7d093494d7cb"
    )

    init {
        plotterHome.mkdirs()
        plotLogsRunning.mkdirs()
        plotLogsFinished.mkdirs()
    }

    fun savePlotJobs(jobs: List<PlotJob>) {
        CoroutineScope(Dispatchers.IO).launch {
            jobsFile.writeText(Json.encodeToString(jobs))
        }
    }

    fun getPlotJobs(): List<PlotJob> {
        if (!jobsFile.exists()) {
            return emptyList()
        }
        var result: List<PlotJob>? = null
        try {
            result = Json.decodeFromString(jobsFile.readText())
        } catch (e: Exception) {
            result = null
            SimpleDialogs.showAlert(
                "Failed to decode jobs",
                "Backing them up to ~/.harryplotter/jobs.bak and resetting jobs.json"
            )
            val backupFile = File(jobsFile.parent + "/jobs.bak")
            backupFile.delete()
            jobsFile.renameTo(backupFile)
            savePlotJobs(listOf())
        }
        return result ?: listOf()
    }

    fun resetConfig() {
        jobsFile.delete()
    }

//    fun saveTime(desc: JobDescription, result: JobResult) {
//    }

//    fun loadTimes(desc: JobDescription): List<JobResult> {
//        return emptyList()
//    }
}
