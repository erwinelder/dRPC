rootProject.name = "drpc"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":drpc")
include(":drpc-core")
include(":drpc-client")
include(":drpc-server")

include(":drpc-processor-core-api")
include(":drpc-processor-client-api")
include(":drpc-processor-server-api")
include(":drpc-processor")
include(":drpc-processor-client")
include(":drpc-processor-server")

include(":sample-client")
include(":sample-server")
include(":sample1-server")
include(":sample2-server")
