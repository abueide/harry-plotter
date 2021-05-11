package com.abysl.harryplotter.data

import kotlinx.serialization.Serializable

@Serializable
data class JobResult(
    // Time in Seconds
    val phaseOneTime: Double = 0.0,
    val phaseTwoTime: Double = 0.0,
    val phaseThreeTime: Double = 0.0,
    val phaseFourTime: Double = 0.0,
    val totalTime: Double = 0.0,
    val copyTime: Double = 0.0,
){
    fun merge(other: JobResult): JobResult {
        return JobResult(
            testTime(phaseOneTime, other.phaseOneTime),
            testTime(phaseTwoTime, other.phaseTwoTime),
            testTime(phaseThreeTime, other.phaseThreeTime),
            testTime(phaseFourTime, other.phaseFourTime),
            testTime(totalTime, other.totalTime),
            testTime(copyTime, other.copyTime)
        )
    }

    fun testTime(time: Double, other: Double): Double{
        return if(time != 0.0) time else other
    }
}