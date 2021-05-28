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

package com.abysl.harryplotter.viewmodel

import com.abysl.harryplotter.model.PlotJob
import com.abysl.harryplotter.util.invoke
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val LOG_REFRESH_DELAY = 1000L
class JobStatusViewModel {
    var logsScope = CoroutineScope(Dispatchers.IO)
    val shownJob: MutableStateFlow<PlotJob?> = MutableStateFlow(null)
    val shownLogs: MutableStateFlow<List<String>> = MutableStateFlow(listOf())

    fun loadJob(job: PlotJob) {
        clearJob()
        shownJob.value = job
        shownJob()?.process?.let {
            it.cache = true
            logsScope.launch {
                while (true){
                    if(it.logs.size > shownLogs().size){
                        shownLogs.value = it.logs
                    }
                    delay(LOG_REFRESH_DELAY)
                }
            }
        }
        shownJob()?.process?.cache = true
    }

    fun clearJob() {
        logsScope.cancel()
        logsScope = CoroutineScope(Dispatchers.IO)
        shownJob.value = null
        shownLogs.value = listOf()
    }

}
