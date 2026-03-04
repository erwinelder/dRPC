plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    alias(libs.plugins.vanniktech.maven.publish)
    `maven-publish`
    signing
}

dependencies {
}

gradlePlugin {
    plugins {
        create("drpcShadowMerge") {
            id = "io.github.erwinelder.drpc.shadow"
            implementationClass = "com.docta.drpc.gradle.DrpcShadowMergePlugin"
            displayName = "dRPC Shadow service-file merge"
            description = "Configures ShadowJar to merge dRPC ServiceLoader descriptors."
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

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(groupId = group.toString(), artifactId = "drpc-gradle-plugin", version = version.toString())

    pom {
        name = "dRPC Gradle Plugin"
        description = "Gradle plugin for dRPC (ShadowJar service descriptor merge)."
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