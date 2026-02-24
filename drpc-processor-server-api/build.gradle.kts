plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":drpc-processor-core-api"))

    // Ktor Server
    implementation(libs.ktor.server.core)
    // Utilities
    implementation(libs.ksp.symbol.processing.api)
}