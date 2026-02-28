import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(project(":drpc"))
//    implementation(project(":drpc-client"))
//    implementation(project(":drpc-server"))
    ksp(project(":drpc-processor"))
//    ksp(project(":drpc-processor-client"))
//    ksp(project(":drpc-processor-server"))

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
    // Test
    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.test.host)
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}