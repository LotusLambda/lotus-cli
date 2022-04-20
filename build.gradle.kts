import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    kotlin("plugin.serialization") version "1.6.10"
    application
}

group = "me.ianrumac"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    mavenLocal()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("com.github.ajalt.clikt:clikt:3.4.1")
    implementation("com.lotuslambda.engine:flow-machine:0-SNAPSHOT")
    implementation("com.lotuslambda.parser:lotus-parser:0.0.1")
    implementation("io.ktor:ktor-server-core:2.0.0")
    implementation("io.ktor:ktor-server-cio:2.0.0")
    implementation("io.ktor:ktor-server-websockets:2.0.0")
    implementation("com.google.zxing:core:3.4.1")
    implementation("com.github.sorleone:TermQRCode:master-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    implementation("com.github.ajalt.mordant:mordant:2.0.0-beta2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}