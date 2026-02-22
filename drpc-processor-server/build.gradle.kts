plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":drpc-core"))

    // Ktor Server
    implementation(libs.ktor.server.core)
    // Utilities
    implementation(libs.ksp.symbol.processing.api)
}