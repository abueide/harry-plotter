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

import com.abysl.harryplotter.chia.ChiaCli
import com.abysl.harryplotter.config.Config
import com.abysl.harryplotter.util.invoke
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainViewModel {

    val jobsListViewModel: JobsListViewModel = JobsListViewModel()
    val jobEditorViewModel: JobEditorViewModel = JobEditorViewModel()
    val jobStatusViewModel: JobStatusViewModel = JobStatusViewModel()

    init {
        jobEditorViewModel.initialized(
            savedCallback = jobsListViewModel::saveJob,
            selectCallback = jobsListViewModel::clearSelected
        )
        jobsListViewModel.plotJobs.value += Config.getPlotJobs()

        jobEditorViewModel.chiaKeys.value += ChiaCli().readKeys()
        jobEditorViewModel.selectedKey.value = jobEditorViewModel.chiaKeys().firstOrNull()

        jobsListViewModel.selectedPlotJob.onEach {
            if (it == null) {
                jobEditorViewModel.clearJob()
                jobStatusViewModel.clearJob()
            } else {
                jobEditorViewModel.loadJob(it)
                jobStatusViewModel.loadJob(it)
            }
        }.launchIn(CoroutineScope(Dispatchers.IO))
        // "Plot Job ${jobsListViewModel.plotJobs.value.size + 1}"
    }
}
