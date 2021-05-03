package com.abysl.harryplotter.chia

import com.abysl.harryplotter.data.ChiaKey
import com.abysl.harryplotter.data.JobDescription
import com.abysl.harryplotter.util.unlines
import javafx.application.Application.launch
import javafx.application.Platform
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.CompletableFuture.runAsync
import java.util.concurrent.TimeUnit

class ChiaCli(val exe: File, val config: File) {

    val chiaHome = config.parentFile.parentFile


    fun createPlot(jobDescription: JobDescription){

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

    /**
     * Usage: runCommand("keys", "show") -> chia.exe keys show
     * @param args
     * @return
     */

    fun runCommand(vararg args: String): List<String> {
        val command: List<String> = listOf(exe.name) + args.toList()
        val proc: Process = ProcessBuilder(command)
            .directory(exe.parentFile)
            .start()
        proc.waitFor(10, TimeUnit.SECONDS)
        val input: InputStream = proc.inputStream
        val err: InputStream = proc.errorStream
        return input.reader().readLines()
    }

    fun runCommandAsync(vararg args: String, outputCallback: (line: String) -> Unit, finishedCallBack: () -> Unit): Process {
        val command: List<String> = listOf(exe.name) + args.toList()
        println(command.unlines())
        val proc = ProcessBuilder(command)
            .directory(exe.parentFile)
            .start()
        val input  = BufferedReader(InputStreamReader(proc.inputStream))
        val err  = BufferedReader(InputStreamReader(proc.errorStream))

        Platform.runLater {
            while (proc.isAlive){
                if(input.ready()){
                    outputCallback(input.readLine())
                }
                if(err.ready()){
                    outputCallback(err.readLine())
                }
            }
            finishedCallBack()
        }
        return proc
    }
}