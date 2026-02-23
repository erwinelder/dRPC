plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":drpc-core"))

    // Ktor Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.websockets)
    // Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.client.content.negotiation)
}