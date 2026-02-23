plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":drpc-processor-core-api")) // TODO: maybe change to api

    // Ktor Server
    implementation(libs.ktor.server.core)
    // Utilities
    implementation(libs.ksp.symbol.processing.api)
}