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
import com.abysl.harryplotter.chia.ChiaLocator
import com.abysl.harryplotter.config.Config
import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.model.PlotJob
import com.abysl.harryplotter.model.records.ChiaKey
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewModel {
    val plotJobs: MutableStateFlow<List<PlotJob>> = MutableStateFlow(listOf())
    val chiaKeys: MutableStateFlow<List<ChiaKey>> = MutableStateFlow(listOf())
    val selectedPlotJob: MutableStateFlow<PlotJob?> = MutableStateFlow(null)
    val selectedKey: MutableStateFlow<ChiaKey?> = MutableStateFlow(null)

    private val jobsListViewModel: JobsListViewModel = JobsListViewModel(this.mainViewModel)

    init {
        val chiaLocator = ChiaLocator(mainBox)
        val exePath = chiaLocator.getExePath()
        Prefs.exePath = exePath.path
        chia = ChiaCli(exePath, chiaLocator.getConfigFile())
        jobsListView.initialized()
        jobEditorView.initialized()
        jobStatusViewController.initialized()
        keys += chia.readKeys()
        jobs += Config.getPlotJobs().map { PlotJob(chia, it) }
        selectedKey = keys.first()
    }
}