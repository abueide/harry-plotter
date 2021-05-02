package com.abysl.harryplotter.data

import com.abysl.harryplotter.util.FileSerializer
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
@JvmRecord
data class JobDescription(
    val name: String,
    @Serializable(with = FileSerializer::class)
    val tempDir: File,
    @Serializable(with = FileSerializer::class)
    val destDir: File,
    val threads: Int, // -1 = default
    val ram: Int, // MiB -1 = default
    val key: ChiaKey, // -1 = default
    val stopAfter: Int,
){
    override fun toString(): String {
        return name
    }
}
