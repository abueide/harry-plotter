package com.abysl.harryplotter.data

data class JobStats(
    var plotsDone: Int = 0,
    val results: MutableList<JobResult> = mutableListOf()
)
