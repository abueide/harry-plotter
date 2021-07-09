@file:UseSerializers(FileSerializer::class)
package com.abysl.harryplotter.model.drives

import com.abysl.harryplotter.util.serializers.FileSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.io.File

@Serializable
@SerialName("DestinationDrive")
class DestDrive(
    override val name: String = "Unnamed Destination Drive",
    override val drivePath: File = File(""),
    override val driveType: DriveType = DriveType.DESTINATION,
    val maxPlotTransfer: Int = 1
) : Drive() {
    override fun deepCopy(): Drive = DestDrive(name, drivePath, driveType, maxPlotTransfer)
}