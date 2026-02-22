plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":drpc-core"))

    implementation(libs.ksp.symbol.processing.api)
}