package com.abysl.harryplotter.chia

import com.abysl.harryplotter.data.ChiaKey
import com.abysl.harryplotter.data.JobDescription
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class ChiaCli(val exe: File, val config: File) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.JavaFx

    val chiaHome = config.parentFile.parentFile


    fun createPlot(jobDescription: JobDescription) {

    }

    fun readKeys(): List<ChiaKey> {
        val keyInput = runCommand("keys", "show")
        val keys = mutableListOf<ChiaKey>()
        for (line in keyInput) {
            if (line.contains("Fingerprint")) {
                keys.add(ChiaKey())
            }
            if (keys.isNotEmpty()) {
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

    fun runCommand(vararg commandArgs: String): List<String> {
        val command: List<String> = listOf(exe.name) + commandArgs.toList()
        val proc: Process = ProcessBuilder(command)
            .directory(exe.parentFile)
            .start()
        proc.waitFor(100, TimeUnit.SECONDS)
        val input: InputStream = proc.inputStream
        val err: InputStream = proc.errorStream
        return input.reader().readLines()
    }

    private val fx = CoroutineScope(Dispatchers.JavaFx)
    private val io = CoroutineScope(Job() + Dispatchers.IO)

    fun runCommandAsyncc(
        vararg commandArgs: String,
        outputCallback: (line: String) -> Unit,
        finishedCallBack: () -> Unit
    ): Process {
        val command: List<String> = listOf(exe.name) + commandArgs.toList()
        val proc = ProcessBuilder(command)
            .directory(exe.parentFile)
            .start()


            io.launch {
                val input = BufferedReader(InputStreamReader(proc.inputStream))
                val err = BufferedReader(InputStreamReader(proc.errorStream))
                while (proc.isAlive) {
                    if (input.ready()) {
                        fx.launch {
                            outputCallback(input.readLine())
                        }
                    }
                    if (err.ready()) {
                        fx.launch {
                            outputCallback(err.readLine())
                        }
                    }
                    delay(10)
                }
                fx.launch {
                    finishedCallBack()
                }
            }
        return proc
    }
}