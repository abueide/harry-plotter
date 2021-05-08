package com.abysl.harryplotter.data

import com.abysl.harryplotter.chia.ChiaCli
import com.abysl.harryplotter.config.Config.io
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.TextArea
import kotlinx.coroutines.*

class JobProcess(val chia: ChiaCli, val logWindow: TextArea, val jobDesc: JobDescription) {
    var proc: Process? = null
    val logs: ObservableList<String> = FXCollections.observableArrayList()

    var state: JobState = JobState()



    fun start() {
        if(proc == null) {
            state.status = RUNNING
            state.running = true

            proc = chia.runCommandAsyncc(
                "plots",
                "create",
                "-k", "32",
                "-a", jobDesc.key.fingerprint,
                "-b", jobDesc.ram.toString(),
                "-r", jobDesc.threads.toString(),
                "-t", jobDesc.tempDir.toString(),
                "-d", jobDesc.destDir.toString(),
                outputCallback = ::parseLine,
                finishedCallBack = ::whenDone
            )
        }
    }

    fun reset() {
        proc?.destroy()
        logs.clear()
        io.launch { deleteTempFiles() }
        state = JobState()
    }

    suspend fun deleteTempFiles(){
        val maxTries = 100
        if(state.plotId.isNotBlank()) {
            jobDesc.tempDir.listFiles()?.forEach {
                if (it.toString().contains(state.plotId) && it.extension == "tmp") {
                    var timeout = 0
                    while (!it.delete() && timeout++ < maxTries) {
                        println("Couldn't delete file, trying again in 100 ms")
                        delay(100)
                    }
                }
                println(" " + it.name)
            }
        }
    }


    fun whenDone() {
        state.plotCount++
        if (state.running && (state.plotCount < jobDesc.plotsToFinish || jobDesc.plotsToFinish == 0)) {
            reset()
            start()
        }
    }

    fun parseLine(line: String) {
        logs.add(line)
        if (state.displayLogs) {
            logWindow.appendText(line + "\n")
        }
        if(line.contains("ID: ")){
            state.plotId = line.split("ID: ")[1]
            println(state.plotId)
        }

        if (line.contains("Starting phase")) {
            state.phase = line
                .split("Starting phase ")[1]
                .split("/")[0]
                .toInt()
        } else if (line.contains("tables")) {
            val split = line.split("tables ")
            state.subphase = line.split("tables ")[1]
        } else if (line.contains("table")) {
            val split = line.split("table ")
            state.subphase = line.split("table ")[1]
        } else if (line.contains("Time for phase")) {
            val phase: Int = line.split("phase ")[1].split(" =")[0].toInt()
            val seconds: Int = line.split("= ")[1].split(" seconds")[0].toInt()
            when (phase) {
                1 -> state.currentResult = state.currentResult.merge(JobResult(phaseOneTime = seconds))
                2 -> state.currentResult = state.currentResult.merge(JobResult(phaseTwoTime = seconds))
                3 -> state.currentResult = state.currentResult.merge(JobResult(phaseThreeTime = seconds))
                4 -> state.currentResult = state.currentResult.merge(JobResult(phaseFourTime = seconds))
            }
        } else if (line.contains("Total time")) {
            val seconds: Int = line.split("= ")[1].split(" seconds")[0].toInt()
            state.currentResult = state.currentResult.merge(JobResult(totalTime = seconds))
        } else if (line.contains("Copy time")) {
            val seconds: Int = line.split("= ")[1].split(" seconds")[0].toInt()
            state.currentResult = state.currentResult.merge(JobResult(copyTime = seconds))
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