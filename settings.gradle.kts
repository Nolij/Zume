pluginManagement {
    repositories {
		mavenLocal()
	    mavenCentral()
	    maven("https://maven.fabricmc.net/")
	    maven("https://maven.glass-launcher.net/babric")
	    maven("https://repo.legacyfabric.net/repository/legacyfabric/")
	    maven("https://maven.wagyourtail.xyz/releases")
	    maven("https://maven.wagyourtail.xyz/snapshots")
        gradlePluginPortal {
	        content {
		        excludeGroup("org.apache.logging.log4j")
	        }
        }
    }
}

buildscript {
	dependencies {
		classpath("org.ow2.asm:asm-tree:9.6")
	}
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
	id("xyz.wagyourtail.unimined") version "1.2.0-SNAPSHOT" apply(false)
	id("com.github.gmazzo.buildconfig") version "5.2.0" apply(false)
	id("com.github.johnrengelman.shadow") version "8.1.1" apply(false)
	id("me.modmuss50.mod-publish-plugin") version "0.4.5" apply(false)
}

rootProject.name = "zume"

include("stubs")
include("common")
include("modern")
include("legacy")
include("primitive")
include("archaic")
include("vintage")
include("neoforge")
include("lexforge")
include("lexforge18")
include("lexforge16")