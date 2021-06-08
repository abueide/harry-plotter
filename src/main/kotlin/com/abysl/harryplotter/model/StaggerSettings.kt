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

package com.abysl.harryplotter.model

import com.abysl.harryplotter.model.jobs.PlotJob
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Serializable
@JvmRecord
data class StaggerSettings(
    val maxFirstStagger: Int = 0,
    val maxOtherStagger: Int = 0,
    val maxTotal: Int = 0,
    val staticStagger: Int = 0,
    val staticIgnore: Boolean = false
) {
    @OptIn(ExperimentalTime::class)
    fun check(lastStart: Instant?, jobs: List<PlotJob>): Boolean {
        val runningJobs by lazy { jobs.filter(PlotJob::isRunning) }
        val time by lazy { Clock.System.now() }
        val staticCheck by lazy {
            if (lastStart != null)
                staticStagger == 0 || (time - lastStart) >= Duration.minutes(staticStagger)
            else
                true
        }

        val firstPhaseCheck by lazy {
            maxFirstStagger == 0 || runningJobs.filter { it.state.phase == 1 }.size < maxFirstStagger
        }
        val otherPhaseCheck by lazy {
            maxOtherStagger == 0 || runningJobs.filter { it.state.phase != 1 }.size < maxOtherStagger
        }
        val totalCheck by lazy { maxTotal == 0 || runningJobs.size < maxTotal }
        return totalCheck &&
            staticCheck &&
            firstPhaseCheck &&
            otherPhaseCheck
    }
}
