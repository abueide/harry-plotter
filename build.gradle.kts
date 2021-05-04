import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.4.32"
    kotlin("plugin.serialization") version "1.4.32"
    id("org.openjfx.javafxplugin") version "0.0.9"
}

group = "com.abysl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.0")
    implementation("com.charleskorn.kaml:kaml:0.31.0")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions {
        jvmTarget = "15"
        useIR = true
    }
}

application {
//    mainModule.set("com.abysl.harryplotter")
    mainClass.set("com.abysl.harryplotter.HarryPlotter")
}

javafx {
    version = "15"
    modules = listOf("javafx.controls", "javafx.web", "javafx.fxml")
}