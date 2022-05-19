import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

group = "me.ianrumac"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    val libs = listOf("flow-machine", "parser-multiplatform")
    for (lib in libs) {
        maven {
            url = uri("https://maven.pkg.github.com/lotuslambda/${lib}")
            credentials {
                username = "ianrumac"
                password = "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"
            }
        }
    }

}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("com.github.ajalt.clikt:clikt:3.4.1")
    implementation("com.lotuslambda.engine:flow-machine:0.1.1-SNAPSHOT")
    implementation("com.lotuslambda.parser:parsermultiplatform:0.1.0")
    implementation("io.ktor:ktor-server-core:1.6.8")
    implementation("io.ktor:ktor-server-cio:1.6.8")
    implementation("io.ktor:ktor-websockets:1.6.8")
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