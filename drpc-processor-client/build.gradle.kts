plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":drpc-processor-core-api")) // TODO: maybe change to api
    implementation(project(":drpc-processor-client-api")) // TODO: maybe change to api

    implementation(libs.ksp.symbol.processing.api)
}