plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":drpc-core"))
    api(project(":drpc-processor-core-api"))

    implementation(libs.ksp.symbol.processing.api)
}