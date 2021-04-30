package com.abysl.harryplotter.data

import java.io.File

@JvmRecord
data class Job(
    val name: String,
    val tempDir: File,
    val destDir: File,
    val key: ChiaKey
){
    override fun toString(): String {
        return name;
    }
}
