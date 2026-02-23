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

include(":drpc-processor-core-api")
include(":drpc-processor-client-api")
include(":drpc-processor-server-api")
include(":drpc-processor")
include(":drpc-processor-client")
include(":drpc-processor-server")

include(":test-client-module")
include(":test-server-module")
