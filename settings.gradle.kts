pluginManagement {
    repositories {
	    mavenCentral()
	    maven("https://maven.taumc.org/releases")
	    maven("https://maven.wagyourtail.xyz/releases")
	    maven("https://maven.wagyourtail.xyz/snapshots")
	    gradlePluginPortal {
		    content {
			    excludeGroup("org.apache.logging.log4j")
		    }
	    }
    }
	
	plugins {
		fun property(name: String): String = extra[name] as? String ?: error("Property ${name} not found")

		id("org.gradle.toolchains.foojay-resolver-convention") version(property("foojay_resolver_convention_version"))
		id("org.taumc.gradle.versioning") version(property("taugradle_version"))
		id("org.taumc.gradle.publishing") version(property("taugradle_version"))
		id("com.gradleup.shadow") version(property("shadow_version"))
		id("xyz.wagyourtail.unimined") version(property("unimined_version"))
		id("com.github.gmazzo.buildconfig") version(property("buildconfig_version"))
		id("ru.vyarus.use-python") version(property("use_python_version"))
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention")
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