package com.abysl.harryplotter.data

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
        if (line.contains("Fingerprint")) {
            return ChiaKey(fingerprint = line.split(": ")[1]).merge(this)
        } else if (line.contains("Master public")) {
            return ChiaKey(publicKey = line.split(": ")[1]).merge(this)
        } else if (line.contains("Farmer public")) {
            return ChiaKey(farmerKey = line.split(": ")[1]).merge(this)
        } else if (line.contains("Pool public")) {
            return ChiaKey(poolKey = line.split(": ")[1]).merge(this)
        }else {
            return this
        }
    }
}
