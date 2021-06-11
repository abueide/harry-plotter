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

import javafx.beans.value.ObservableValue
import javafx.beans.value.WritableValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun <T> WritableValue<T>.bind(stateFlow: StateFlow<T>): BindingConverter {
    return ObservableToFlowBinding(this, stateFlow)
}

fun <T> MutableStateFlow<T>.bind(observable: ObservableValue<T>): BindingConverter {
    return FlowToObservableBinding(this, observable)
}

fun <T, U> T.bindBidirectional(stateFlow: MutableStateFlow<U>): BindingConverter
        where T : ObservableValue<U>,
              T : WritableValue<U> {
    return BiDirectionalBinding(this.bind(stateFlow), stateFlow.bind(this))
}

fun <T, U> MutableStateFlow<U>.bindBidirectional(observable: T): BindingConverter
        where T : ObservableValue<U>,
              T : WritableValue<U> {
    return BiDirectionalBinding(this.bind(observable), observable.bind(this))
}
