package com.abysl.harryplotter.chia

import com.abysl.harryplotter.data.ChiaKey
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KSuspendFunction0

class ChiaCli(val exe: File, val config: File) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.JavaFx

    val chiaHome = config.parentFile.parentFile


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
        return keys
    }

    /**
     * Usage: runCommand("keys", "show") -> chia.exe keys show
     * @param args
     * @return
     */

    fun runCommand(vararg commandArgs: String): List<String> {
        val command: List<String> = listOf(exe.path) + commandArgs.toList()
        val proc: Process = ProcessBuilder(command)
            .start()
        proc.waitFor(100, TimeUnit.SECONDS)
        val input: InputStream = proc.inputStream
        val err: InputStream = proc.errorStream
        return input.reader().readLines()
    }


    fun runCommandAsync(
        ioDelay: Long = 10,
        outputCallback: (String) -> Unit,
        completedCallback: () -> Unit,
        vararg commandArgs: String,
    ): Process {
        val command: List<String> = listOf(exe.name) + commandArgs.toList()
        val proc: Process = ProcessBuilder(command)
            .directory(exe.parentFile)
            .start()
        val input = BufferedReader(InputStreamReader(proc.inputStream))
        val err = BufferedReader(InputStreamReader(proc.errorStream))
        CoroutineScope(Dispatchers.IO).launch {
            while (proc.isAlive) {
                input.lines().forEach {
                    outputCallback(it)
                }
                err.lines().forEach {
                    outputCallback(it)
                }
                delay(ioDelay)
            }
            input.close()
            err.close()
            completedCallback()
        }
        return proc
    }

}