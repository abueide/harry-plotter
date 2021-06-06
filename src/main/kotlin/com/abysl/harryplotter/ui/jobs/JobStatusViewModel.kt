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

package com.abysl.harryplotter.ui.jobs

import com.abysl.harryplotter.model.PlotJob
import kotlinx.coroutines.flow.MutableStateFlow

class JobStatusViewModel {
    val shownJob: MutableStateFlow<PlotJob?> = MutableStateFlow(null)

    fun loadJob(job: PlotJob) {
        clearJob()
        shownJob.value = job
    }

    fun clearJob() {
//        shownJob()?.process?.cache = false
        shownJob.value = null
    }
}
