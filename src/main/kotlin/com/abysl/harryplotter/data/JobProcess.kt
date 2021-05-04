package com.abysl.harryplotter.data

import com.abysl.harryplotter.chia.ChiaCli
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.TextArea

class JobProcess(val chia: ChiaCli, val logWindow: TextArea, val jobDesc: JobDescription) {
    var status: String = STOPPED
    var running: Boolean = false
    var phase: Int = 1
    var subphase: String = ""
    var currentResult: JobResult = JobResult()
    val results: MutableList<JobResult> = mutableListOf()
    var percentage: Double = 0.0
    var stopwatch: Int = 0;
    var plotsDone = 0
    var proc: Process? = null
    val logs: ObservableList<String> = FXCollections.observableArrayList()
    var plotCount: Int = 0
    var displayLogs = false

    fun start(){
        status = RUNNING
        running = true

        proc = chia.runCommandAsync(
            "plots",
            "create",
            "-k 32",
            "-a ${jobDesc.key.fingerprint}",
            "-b ${jobDesc.ram}",
            "-r ${jobDesc.threads}",
            "-t", jobDesc.tempDir.toString(),
            "-d", jobDesc.destDir.toString(),
            outputCallback = ::parseLine,
            finishedCallBack = ::whenDone)
    }

    fun reset(){
        status = STOPPED
        running = false
        proc?.destroyForcibly()
        proc = null
        stopwatch = 0
        currentResult = JobResult()
        phase = 1
        subphase = ""
        percentage = 0.0
        logs.clear()
    }

    fun whenDone(){
        plotCount++
        if(running && (plotCount < jobDesc.plotsToFinish || jobDesc.plotsToFinish == 0)){
            reset()
            start()
        }
    }

    fun parseLine(line: String){
        logs.add(line)
        if(displayLogs) {
            logWindow.appendText(line + "\n")
        }

        if(line.isNotBlank()) {
            if (line.contains("Starting phase")) {
                phase = line.split("Starting phase ")[0].toInt()
            } else if (line.contains("tables")) {
                subphase = line.split("tables ")[1]
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
        }
    }

    override fun toString(): String {
        return if(running)
            "$jobDesc - $percentage%"
        else
            jobDesc.toString()
    }

    companion object {
        val STOPPED = "Stopped"
        val RUNNING = "Running"
        val ERROR = "Error"

        val PHASEONE = "PHASEONE"
        val PHASETWO = "PHASETWO"
        val PHASETHREE = "PHASETHREE"
        val PHASEFOUR = "PHASEFOUR"
    }
}