group = "io.github.erwinelder"
version = "0.3.1"

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply  false
    alias(libs.plugins.kotlin.plugin.serialization) apply false
    alias(libs.plugins.vanniktech.maven.publish) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.plugin.compose) apply false
    alias(libs.plugins.jetbrains.compose) apply false
}