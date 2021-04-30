package com.abysl.harryplotter.data

data class ChiaKey(
    val nickname: String,
    val fingerprint: String,
    val publicKey: String,
    val farmerKey: String,
    val poolKey: String
){
    override fun toString(): String {
        return fingerprint;
    }
}
