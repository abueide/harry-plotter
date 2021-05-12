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
) {
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

    fun testTime(time: Double, other: Double): Double {
        return if (time != 0.0) time else other
    }
}
