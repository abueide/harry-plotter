package com.abysl.harryplotter.chia

import com.abysl.harryplotter.data.ChiaKey
import com.abysl.harryplotter.data.Job
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

class ChiaCli(val exe: File, val config: File) {

    val chiaHome = config.parentFile.parentFile
    val plotterHome = File(System.getProperty("user.home") + "/.harryplotter/")

    init {
        if(!plotterHome.exists()){
            plotterHome.mkdirs()
        }
    }

    fun createPlot(job: Job){

    }

    fun readKeys(): List<ChiaKey> {
        val keyInput = runCommand("keys", "show")
        val keys = mutableListOf<ChiaKey>()
        for(line in keyInput){
            if(line.contains("Fingerprint")){
                keys.add(ChiaKey())
            }
            if(keys.isNotEmpty()) {
                keys[keys.size - 1] = keys[keys.size - 1].parseLine(line)
            }
        }
        keys.forEach { _ -> println() }
        return keys
    }

    fun runCommand(vararg args: String): List<String> {
        val command: List<String> = listOf(exe.name) + args.toList()
        println(command)
        val proc: Process = ProcessBuilder(command)
            .directory(exe.parentFile)
            .start()
        proc.waitFor(10, TimeUnit.SECONDS)
        val input: InputStream = proc.inputStream
        val err: InputStream = proc.errorStream
        return input.reader().readLines()
    }

    fun runCommandAsync(vararg args: String, outputCallback: (line: String) -> Unit){
//        ProcessBuilder()
//            .directory(exe.parentFile)
//            .start()
    }
}