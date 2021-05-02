package com.abysl.harryplotter.data

import kotlinx.serialization.Serializable

@Serializable
@JvmRecord
data class JobResult(
    // Time in Seconds
    val phaseOneTime: Int = 0,
    val phaseTwoTime: Int = 0,
    val phaseThreeTime: Int = 0,
    val phaseFourTime: Int = 0,
    val totalTime: Int = 0,
    val copyTime: Int = 0,
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

    fun testTime(time: Int, other: Int): Int{
        return if(time != 0) time else other
    }
}