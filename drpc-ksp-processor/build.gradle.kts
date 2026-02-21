group = "io.github.erwinelder"
version = "0.3.1"

plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":library"))

    implementation(libs.ksp.symbol.processing.api)
}