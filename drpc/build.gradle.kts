import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.vanniktech.maven.publish)
    `maven-publish`
    signing
}

kotlin {
    jvm()
    androidLibrary {
        namespace = "com.docta.drpc"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava()
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosArm64()
    macosX64()

    sourceSets {
        commonMain.dependencies {
            api(project(":drpc-core"))
            api(project(":drpc-client"))
        }
        jvmMain.dependencies {
            api(project(":drpc-server"))
        }
    }
}

signing {
    useInMemoryPgpKeys(
        System.getenv("GPG_SIGNING_KEY"),
        System.getenv("GPG_SIGNING_PASSWORD")
    )
    sign(publishing.publications)
}

mavenPublishing { // TODO
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "drpc", version.toString())

    pom {
        name = "dRPC"
        description = "Docta Remote Procedure call build on top of Kotlin/Ktor."
        inceptionYear = "2025"
        url = "https://github.com/erwinelder/dRPC/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "erwinelder"
                name = "Erwin Elder"
                url = "https://github.com/erwinelder/"
                email = "erwineldermail@gmail.com"
                organization = ""
                organizationUrl = ""
            }
        }
        scm {
            url = "https://github.com/erwinelder/dRPC/"
            connection = "scm:git:git://github.com/erwinelder/dRPC.git"
            developerConnection = "scm:git:ssh://git@github.com/erwinelder/dRPC.git"
        }
    }
}