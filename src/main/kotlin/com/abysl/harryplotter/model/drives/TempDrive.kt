@file:UseSerializers(FileSerializer::class)
package com.abysl.harryplotter.model.drives

import com.abysl.harryplotter.model.StaggerSettings
import com.abysl.harryplotter.util.serializers.FileSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import java.io.File

@Serializable
class TempDrive(
    override val name: String = "Unnamed Temp Drive",
    override val drivePath: File = File(""),
    @Transient
    override val driveType: DriveType = DriveType.TEMP,
    val staggerSettings: StaggerSettings = StaggerSettings(),
) : Drive() {
    override fun deepCopy(): Drive = TempDrive(name, drivePath, driveType, staggerSettings)
}
