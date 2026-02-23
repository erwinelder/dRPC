import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(project(":drpc-core"))
    implementation(project(":drpc-client"))
    implementation(project(":drpc-server"))
    ksp(project(":drpc-processor"))

    // Ktor Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    // Ktor Client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio.jvm)
    // Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.client.content.negotiation)
    // Koin
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xcontext-parameters"))
}