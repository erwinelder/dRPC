plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.vanniktech.maven.publish)
    `maven-publish`
    signing
}

dependencies {
    api(project(":drpc-processor-core-api"))

    // Ktor Server
    implementation(libs.ktor.server.core)
    // Utilities
    implementation(libs.ksp.symbol.processing.api)
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

    coordinates(groupId = group.toString(), artifactId = "drpc-processor-server-api", version = version.toString())

    pom {
        name = "dRPC Processor Server API"
        description = "Docta Remote Procedure Call (Processor Server API) built on top of Kotlin/Ktor."
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