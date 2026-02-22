import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":drpc-core"))

    // Ktor Server
    implementation(libs.ktor.server.core) // TODO: remove if needed
    // Utilities
    implementation(libs.ksp.symbol.processing.api)
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xcontext-parameters"))
}