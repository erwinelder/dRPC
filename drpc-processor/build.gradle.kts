import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.vanniktech.maven.publish)
    `maven-publish`
    signing
}

dependencies {
    api(project(":drpc-processor-core-api"))
    api(project(":drpc-processor-client-api"))
    api(project(":drpc-processor-server-api"))

    // Utilities
    implementation(libs.ksp.symbol.processing.api)
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xcontext-parameters"))
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

    coordinates(groupId = group.toString(), artifactId = "drpc-processor", version = version.toString())

    pom {
        name = "dRPC Processor"
        description = "Docta Remote Procedure call built on top of Kotlin/Ktor."
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