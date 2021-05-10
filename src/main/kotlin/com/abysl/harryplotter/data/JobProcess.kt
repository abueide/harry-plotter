package com.abysl.harryplotter.data

import com.abysl.harryplotter.chia.ChiaCli
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.TextArea
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import java.io.File

class JobProcess(val chia: ChiaCli, val logWindow: TextArea, val jobDesc: JobDescription) {
    var proc: Process? = null
    val logs: ObservableList<String> = FXCollections.observableArrayList()

    var state: JobState = JobState()


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
        proc?.destroyForcibly()
        deleteTempFiles(id, block)
        logs.clear()
        state = JobState()
    }

    private fun deleteTempFiles(plotId: String, block: Boolean) {
        if (plotId.isNotBlank()) {
            val files = jobDesc.tempDir.listFiles()
                ?.filter { it.toString().contains(state.plotId) && it.extension == "tmp" }
                ?.map { deleteFile(it) }
            runBlocking {
                files?.forEach { it.await() }
            }
        }
    }

    private fun deleteFile(file: File, delayTime: Long = 100, maxTries: Int = 100) =
        CoroutineScope(Dispatchers.IO).async {
            var timeout = 0;
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
        state.plotCount++
        stop()
        if (state.running && (state.plotCount < jobDesc.plotsToFinish || jobDesc.plotsToFinish == 0)) {
            start()
        }
    }

    fun parseLine(line: String) {
        logs.add(line)
        if (state.displayLogs) {
            CoroutineScope(Dispatchers.JavaFx).launch {
                logWindow.appendText(line + "\n")
            }
        }
        if (line.contains("ID: ")) {
            state.plotId = line.split("ID: ")[1]
        }

        if (line.contains("Starting phase")) {
            state.phase = line
                .split("Starting phase ")[1]
                .split("/")[0]
                .toInt()
            println(state.phase)
        } else if (line.contains("tables")) {
            val split = line.split("tables ")
            state.subphase = line.split("tables ")[1]
            println(state.subphase)
        } else if (line.contains("table")) {
            val split = line.split("table ")
            state.subphase = line.split("table ")[1]
            println(state.subphase)
        } else if (line.contains("Time for phase")) {
            val phase: Int = line.split("phase ")[1].split(" =")[0].toInt()
            val seconds: Int = line.split("= ")[1].split(" seconds")[0].toInt()
            when (phase) {
                1 -> state.currentResult = state.currentResult.merge(JobResult(phaseOneTime = seconds))
                2 -> state.currentResult = state.currentResult.merge(JobResult(phaseTwoTime = seconds))
                3 -> state.currentResult = state.currentResult.merge(JobResult(phaseThreeTime = seconds))
                4 -> state.currentResult = state.currentResult.merge(JobResult(phaseFourTime = seconds))
            }
            println(state.currentResult)
        } else if (line.contains("Total time")) {
            val seconds: Int = line.split("= ")[1].split(" seconds")[0].toInt()
            state.currentResult = state.currentResult.merge(JobResult(totalTime = seconds))
            println(state.currentResult)
        } else if (line.contains("Copy time")) {
            val seconds: Int = line.split("= ")[1].split(" seconds")[0].toInt()
            state.currentResult = state.currentResult.merge(JobResult(copyTime = seconds))
            println(state.currentResult)
        }
    }

    override fun toString(): String {
        return if (state.running)
            "$jobDesc - ${state.percentage}%"
        else
            jobDesc.toString()
    }

    companion object {
        val STOPPED = "Stopped"
        val RUNNING = "Running"
        val ERROR = "Error"
    }
}