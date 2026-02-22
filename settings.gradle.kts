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

include(":drpc-core")
include(":drpc-client")
include(":drpc-server")
include(":drpc-processor-core")
include(":drpc-processor-client")
include(":drpc-processor-server")

include(":test-module")
