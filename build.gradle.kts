import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.5.0"
    kotlin("plugin.serialization") version "1.5.0"
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("org.beryx.runtime") version "1.12.4"
}

group = "com.abysl"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.5.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.0")
}

val jvmOptions = listOf("-Xms256m", "-Xmx2048m")//, "--illegal-access=permit")

application {
    applicationName = "Harry Plotter"
    mainClass.set("com.abysl.harryplotter.MainKt")
    applicationDefaultJvmArgs = jvmOptions
}

javafx {
    version = "16"
    modules = listOf("javafx.base", "javafx.controls","javafx.fxml", "javafx.web")
}


tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "16"
    }
}

val currentOs = org.gradle.internal.os.OperatingSystem.current()

runtime {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
//    modules.set(listOf("java.desktop"))

    jpackage {
        installerOptions = listOf("--resource-dir", "src/main/resources", "--vendor", "Abysl")
        if (currentOs.isWindows) {
            installerOptions = installerOptions + listOf("--win-per-user-install", "--win-dir-chooser", "--win-menu")
        } else if (currentOs.isLinux) {
        } else if (currentOs.isMacOsX) {
        }
        installerName = project.application.applicationName
        imageName = project.application.applicationName
        imageOptions = listOf("--icon", "src/main/resources/com/abysl/harryplotter/icons/snitch.ico", "--win-console")
        appVersion = project.version.toString()
    }

    launcher {
        noConsole = true
    }
}