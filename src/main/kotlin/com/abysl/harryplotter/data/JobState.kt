package com.abysl.harryplotter.data

import javafx.collections.FXCollections
import javafx.collections.ObservableList

data class JobState(
    var running: Boolean = false,
    var phase: Int = 1,
    var subphase: String = "",
    var currentResult: JobResult = JobResult(),
    val results: MutableList<JobResult> = mutableListOf(),
    var percentage: Double = 0.0,
    var stopwatch: Int = 0,
    var displayLogs: Boolean = false,
    var plotId: String = "",
    var status: String = JobProcess.STOPPED
)

