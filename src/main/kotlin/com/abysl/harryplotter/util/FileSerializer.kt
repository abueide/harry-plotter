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

package com.abysl.harryplotter.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.io.File

object FileSerializer : KSerializer<File> {
    override val descriptor = PrimitiveSerialDescriptor("File", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): File {
        return File(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: File) {
        encoder.encodeString(value.absolutePath)
    }
}

object FileFlowSerializer : KSerializer<MutableStateFlow<File>> {
    override val descriptor = PrimitiveSerialDescriptor("FileFlow", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): MutableStateFlow<File> {
        return MutableStateFlow(File(decoder.decodeString()))
    }

    override fun serialize(encoder: Encoder, value: MutableStateFlow<File>) {
        encoder.encodeString(value.value.absolutePath)
    }
}
