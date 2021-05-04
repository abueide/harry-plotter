package com.abysl.harryplotter.data

import com.abysl.harryplotter.util.FileSerializer
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class JobDescription(
    val name: String,
    @Serializable(with = FileSerializer::class)
    val tempDir: File,
    @Serializable(with = FileSerializer::class)
    val destDir: File,
    val threads: Int,
    val ram: Int,
    val key: ChiaKey,
    val plotsToFinish: Int, // -1  = keep going forever
){
    override fun toString(): String {
        return name
    }
}
