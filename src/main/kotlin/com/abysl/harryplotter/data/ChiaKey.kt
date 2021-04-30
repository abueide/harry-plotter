package com.abysl.harryplotter.data

data class ChiaKey(
    val fingerprint: String,
    val publicKey: String,
    val poolKey: String,
    val recvAddress: String
){
    override fun toString(): String {
        return fingerprint;
    }
}
