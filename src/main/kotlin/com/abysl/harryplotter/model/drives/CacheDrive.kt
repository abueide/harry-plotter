@file:UseSerializers(FileSerializer::class)
package com.abysl.harryplotter.model.drives

import com.abysl.harryplotter.util.serializers.FileSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.io.File

@Serializable
class CacheDrive(
    override val name: String = "Unnamed Cache Drive",
    override val drivePath: File = File(""),
    override val driveType: DriveType = DriveType.CACHE
) : Drive() {

    override fun deepCopy(): Drive {
        return CacheDrive(name, drivePath, driveType)
    }

    fun getPlotFiles(): List<File> {
        return drivePath.listFiles()?.filter { !it.name.startsWith(".") && it.extension == "plot" } ?: emptyList()
    }
}