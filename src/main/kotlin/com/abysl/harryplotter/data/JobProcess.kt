package com.abysl.harryplotter.data

import com.abysl.harryplotter.chia.ChiaCli
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.TextArea
import java.lang.Thread.sleep

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
        proc?.destroyForcibly()
        logs.clear()
        jobDesc.tempDir.listFiles()?.forEach {
            if(it.toString().contains(state.plotId) && it.extension == "tmp"){
                while(!it.delete()){
                    println("Couldn't delete file, trying again in 100 ms")
                    sleep(100)
                }
                print("DELETING: ")
            }else {
                print("KEEPING:")
            }
            println(" " + it.name)
        }
        state = JobState()
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
        /*
        if (line.contains("Starting phase")) {
            phase = line
                .split("Starting phase ")[1]
                .split("/")[0]
                .toInt()
        } else if (line.contains("tables")) {
//            subphase = line.split("tables ")[1]
        } else if (line.contains("table")) {
            subphase = line.split("table ")[1]
        } else if (line.contains("Time for phase")) {
            val phase: Int = line.split("phase ")[1].split(" =")[0].toInt()
            val seconds: Int = line.split("= ")[1].split(" seconds")[0].toInt()
            when (phase) {
                1 -> currentResult = currentResult.merge(JobResult(phaseOneTime = seconds))
                2 -> currentResult = currentResult.merge(JobResult(phaseTwoTime = seconds))
                3 -> currentResult = currentResult.merge(JobResult(phaseThreeTime = seconds))
                4 -> currentResult = currentResult.merge(JobResult(phaseFourTime = seconds))
            }
        } else if (line.contains("Total time")) {
            val seconds: Int = line.split("= ")[1].split(" seconds")[0].toInt()
            currentResult = currentResult.merge(JobResult(totalTime = seconds))
        } else if (line.contains("Copy time")) {
            val seconds: Int = line.split("= ")[1].split(" seconds")[0].toInt()
            currentResult = currentResult.merge(JobResult(copyTime = seconds))
        }

         */
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