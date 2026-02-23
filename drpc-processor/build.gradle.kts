import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":drpc-processor-core-api"))
    implementation(project(":drpc-processor-client-api"))
    implementation(project(":drpc-processor-server-api"))

    // Utilities
    implementation(libs.ksp.symbol.processing.api)
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xcontext-parameters"))
}