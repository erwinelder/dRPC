group = "io.github.erwinelder"
version = "0.1.0"

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.vanniktech.maven.publish)
    `maven-publish`
    signing
}

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.websockets)
    // Ktor Client
    implementation(libs.ktor.client.cio.jvm)
    implementation(libs.ktor.client.websockets)
    // Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.client.content.negotiation)
    // Utilities
    implementation(libs.logback.classic)
}

signing {
    useInMemoryPgpKeys(
        System.getenv("GPG_SIGNING_KEY"),
        System.getenv("GPG_SIGNING_PASSWORD")
    )
    sign(publishing.publications)
}

mavenPublishing {
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

tasks.named("shadowJar") {
    enabled = false
}