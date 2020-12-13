import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10" // gradle plugin(org.jetbrains.kotlin.plugin.serialization) 相当
}

group = "dev.ishikawa"
version = "1.0-SNAPSHOT"

repositories {
    gradlePluginPortal()
    mavenCentral()
    jcenter()
    maven("https://kotlin.bintray.com/kotlinx")
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.20")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")

    implementation("com.github.javafaker:javafaker:1.0.2")

    implementation("com.github.kittinunf.fuel:fuel:2.3.0")
    implementation("com.github.kittinunf.fuel:fuel-coroutines:2.3.0")
    implementation("com.github.kittinunf.fuel:fuel-kotlinx-serialization:2.3.0")

    implementation("com.github.kittinunf.result:result:3.1.0")
    implementation("com.github.kittinunf.result:result-coroutines:3.1.0")

    // for httpclient
    implementation("io.ktor:ktor-client-core:1.4.0")
    implementation("io.ktor:ktor-client-apache:1.4.0")

    // for mock server
    implementation("io.ktor:ktor-server-core:1.4.0")
    implementation("io.ktor:ktor-server-netty:1.4.0")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}
//
//val compileKotlin: KotlinCompile by tasks
//compileKotlin.kotlinOptions {
//    freeCompilerArgs = listOf("-Xinline-classes")
//}

