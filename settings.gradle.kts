pluginManagement {
    repositories {
	    gradlePluginPortal()
    }
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version("0.9.0")
}

rootProject.name = "zume"

include("stubs")
include("api")
include("modern")
include("legacy")
include("primitive")
include("archaic")
include("vintage")
include("neoforge")
include("lexforge")
include("lexforge18")
include("lexforge16")
include(":integration:embeddium")