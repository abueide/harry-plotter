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

import com.abysl.harryplotter.chia.ChiaCli
import com.abysl.harryplotter.data.ChiaKey
import com.abysl.harryplotter.data.JobProcess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.serialization.Serializable

@Serializable
object DataModel {
    lateinit var chia: ChiaCli

    var jobsFlow: MutableStateFlow<List<JobProcess>> = MutableStateFlow(listOf())
    var keysFlow: MutableStateFlow<List<ChiaKey>> = MutableStateFlow(listOf())
    var selectedJobFlow: MutableStateFlow<JobProcess?> = MutableStateFlow(null)
    var selectedKeyFlow: MutableStateFlow<ChiaKey?> = MutableStateFlow(null)

    var jobs: List<JobProcess>
        get() = jobsFlow.value
        set(jobsList) {
            jobsFlow.value = jobsList
        }
    var keys: List<ChiaKey>
        get() = keysFlow.value
        set(keysList) {
            keysFlow.value = keysList
        }
    var selectedJob: JobProcess?
        get() = selectedJobFlow.value
        set(job) {
            selectedJobFlow.value = job
        }
    var selectedKey: ChiaKey?
        get() = selectedKeyFlow.value
        set(key) {
            selectedKeyFlow.value = key
        }
}