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

import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.5.10"
    kotlin("plugin.serialization") version "1.5.10"
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("org.beryx.runtime") version "1.12.5"
    id("io.gitlab.arturbosch.detekt") version "1.17.0-RC2"
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
}

val jvmOptions = listOf("-Xms256m", "-Xmx2048m", "-Dprism.order=sw")
val currentOs: OperatingSystem = OperatingSystem.current()
val console = false

group = "com.abysl"
version = "1.1.7"
// version = File("src/main/resources/com/abysl/harryplotter/version.txt").readText()

repositories {
    mavenCentral()
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.17.0-RC2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
    implementation("io.ktor:ktor-client-core:1.5.4")
    implementation("io.ktor:ktor-client-java:1.5.4")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.kordamp.ikonli:ikonli-core:12.2.0")
    implementation("org.kordamp.ikonli:ikonli-javafx:12.2.0")
    implementation("org.kordamp.ikonli:ikonli-fontawesome5-pack:12.2.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "16"
    }
}

tasks.test {
    useJUnitPlatform()
}

application {
    applicationName = "Harry Plotter"
    mainClass.set("com.abysl.harryplotter.HarryPlotterKt")
    applicationDefaultJvmArgs = jvmOptions
}

javafx {
    version = "17-ea+11"
    modules = listOf("javafx.base", "javafx.controls", "javafx.fxml", "javafx.web")
}

val resourcesPath = "build/resources/"
runtime {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
//    modules.set(listOf("java.desktop"))
    launcher {
        noConsole = !console
        // jvmArgs.addAll(jvmOptions)
    }

    jpackage {
        appVersion = project.version.toString()
        imageName = project.application.applicationName
        installerName = "harry-plotter-setup"
        installerOptions = listOf(
            "--resource-dir", resourcesPath,
            "--vendor", "Abysl",
            "--description", "You're a farmer, Harry!",
        )

        if (currentOs.isWindows) {
            installerType = "exe"
            installerOptions = installerOptions + listOf("--win-per-user-install", "--win-dir-chooser", "--win-menu")
            imageOptions = listOf("--icon", "$resourcesPath/main/com/abysl/harryplotter/icons/snitch.ico")
            if (console) {
                imageOptions = imageOptions + listOf("--win-console")
            }
        } else if (currentOs.isLinux) {
            installerType = "deb"
            installerOptions = installerOptions + listOf(
                "--linux-shortcut",
            )
            imageOptions = listOf("--icon", "$resourcesPath/main/com/abysl/harryplotter/icons/snitch.png")
        } else if (currentOs.isMacOsX) {
            installerType = "pkg"
            imageOptions = listOf("--icon", "$resourcesPath/main/com/abysl/harryplotter/icons/snitch.icns")
        }
    }
}

detekt {
    buildUponDefaultConfig = true // preconfigure defaults
    allRules = false // activate all available (even unstable) rules.
    config =
        files("$projectDir/config/detekt/detekt.yml") // point to your custom config defining rules to run, overwriting default behavior
    baseline = file("$projectDir/config/detekt/baseline.xml") // a way of suppressing issues before introducing detekt

    reports {
        html.enabled = true // observe findings in your browser with structure and code snippets
        xml.enabled = true // checkstyle like format mainly for integrations like Jenkins
        txt.enabled = true // similar to the console output, contains issue signature to manually edit baseline files
        // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations with Github Code Scanning
        sarif.enabled = true
    }
}
