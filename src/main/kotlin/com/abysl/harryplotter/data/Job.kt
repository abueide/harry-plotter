package com.abysl.harryplotter.data

import java.io.File

data class Job(
    val name: String,
    val tempDir: File,
    val destDir: File,
    val key: ChiaKey,
    var running: Boolean = false,
    var status: String = "Not Running"
){

    fun  stop(){
        running = false
        status = NOT_RUNNING
    }
    override fun toString(): String {
        return name;
    }

    companion object {
        val NOT_RUNNING = "Not Running"
    }
}
