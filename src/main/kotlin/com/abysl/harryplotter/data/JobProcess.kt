package com.abysl.harryplotter.data

import com.abysl.harryplotter.chia.ChiaCli

class JobProcess(val chia: ChiaCli, val jobDescription: JobDescription) {
    var status: String = STOPPED
    var running: Boolean = false
    var phase: Int = 1
    var subphase: String = ""
    var result: JobResult = JobResult()
    var percentage: Double = 0.0
    var stopwatch: Int = 0;

    fun start(){
        status = RUNNING
        running = true
    }

    fun stop(){
        status = STOPPED
        running = false
        stopwatch = 0
    }

    fun parseLine(line: String){
        if(line.contains("phase")){
            phase = line.split("Starting phase ")[0].toInt()
        }else if(line.contains("tables")){
            subphase = line.split("tables ")[1]
        }else if(line.contains("table")){
            subphase = line.split("table ")[1]
        }else if(line.contains("Time for phase")){
            val phase: Int  = line.split("phase ")[1].split(" =")[0].toInt()
            val seconds: Int = line.split("= ")[1].split(" seconds")[0].toInt()
            when(phase){
                1 -> result = result.merge(JobResult(phaseOneTime = seconds))
                2 -> result = result.merge(JobResult(phaseOneTime = seconds))
                3 -> result = result.merge(JobResult(phaseOneTime = seconds))
                4 -> result = result.merge(JobResult(phaseOneTime = seconds))
            }
        }else if(line.contains("Total time")){
            val seconds: Int = line.split("= ")[1].split(" seconds")[0].toInt()
            result = result.merge(JobResult(totalTime = seconds))
        }else if(line.contains("Copy time")){
            val seconds: Int = line.split("= ")[1].split(" seconds")[0].toInt()
            result = result.merge(JobResult(copyTime = seconds))
        }
    }

    override fun toString(): String {
        return if(running)
            "$jobDescription - $percentage%"
        else
            jobDescription.toString()
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