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

package com.abysl.harryplotter.model.jobs

import kotlinx.serialization.Serializable

@Serializable
data class ChiaKey(
    val nickname: String = "",
    val fingerprint: String = "",
    val publicKey: String = "",
    val farmerKey: String = "",
    val poolKey: String = ""
) {
    override fun toString(): String {
        return nickname.ifBlank { fingerprint }
    }

    fun merge(other: ChiaKey): ChiaKey {
        return ChiaKey(
            nickname.ifBlank { other.nickname },
            fingerprint.ifBlank { other.fingerprint },
            publicKey.ifBlank { other.publicKey },
            farmerKey.ifBlank { other.farmerKey },
            poolKey.ifBlank { other.poolKey },
        )
    }

    fun parseLine(line: String): ChiaKey {
        try {
            if (line.contains("Fingerprint")) {
                return ChiaKey(fingerprint = line.split(": ").last()).merge(this)
            } else if (line.contains("Master public")) {
                return ChiaKey(publicKey = line.split(": ").last()).merge(this)
            } else if (line.contains("Farmer public")) {
                return ChiaKey(farmerKey = line.split(": ").last()).merge(this)
            } else if (line.contains("Pool public")) {
                return ChiaKey(poolKey = line.split(": ").last()).merge(this)
            } else {
                return this
            }
        } catch (e: NoSuchElementException) {
            println("WARNING: Fix line parser")
            println(e)
        }
        return ChiaKey()
    }
}
