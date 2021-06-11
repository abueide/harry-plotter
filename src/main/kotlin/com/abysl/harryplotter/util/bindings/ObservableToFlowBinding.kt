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

package com.abysl.harryplotter.util.bindings

import javafx.beans.value.WritableValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch

class ObservableToFlowBinding<T>(val writableValue: WritableValue<T>, val stateFlow: StateFlow<T>) : BindingConverter {

    init {
        start()
    }

    var job: Job? = null

    override fun start() {
        job = stateFlow.onEach { newValue ->
            CoroutineScope(Dispatchers.JavaFx).launch {
                if (writableValue.value != newValue) {
                    writableValue.value = newValue
                }
            }
        }.launchIn(CoroutineScope(Dispatchers.IO))
    }

    override fun stop() {
        job?.cancel()
    }
}
