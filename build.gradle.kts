import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.5.0"
    id("org.openjfx.javafxplugin") version "0.0.9"
}

group = "com.abysl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "16"
}

application {
//    mainModule.set("com.abysl.harryplotter")
    mainClass.set("com.abysl.harryplotter.HarryPlotter")
}

javafx {
    version = "16"
    modules = listOf("javafx.controls", "javafx.web", "javafx.fxml")
}