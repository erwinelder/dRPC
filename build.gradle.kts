group = "com.docta.dRPC"
version = "1.0"

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.ktor)
}

kotlin {
    jvmToolchain(17)
}

allprojects {
    repositories {
        mavenCentral()
    }
}

dependencies {
    // Ktor Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.websockets)
    // Ktor Client
    implementation(libs.ktor.client.cio.jvm)
    implementation(libs.ktor.client.websockets)
    // Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.client.content.negotiation)
    // Utilities
    implementation(libs.logback.classic)
}