plugins {
	`kotlin-dsl`
}

repositories {
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

fun DependencyHandler.plugin(id: String, version: String) {
	this.implementation(group = id, name = "$id.gradle.plugin", version = version)
}

dependencies {
	// https://central.sonatype.com/artifact/org.ow2.asm/asm-tree
	implementation("org.ow2.asm:asm-tree:9.7")

	// https://central.sonatype.com/artifact/org.apache.ant/ant
	implementation("org.apache.ant:ant:1.10.14")
	
	// https://central.sonatype.com/artifact/com.guardsquare/proguard-base
	implementation("com.guardsquare:proguard-base:7.4.2")
	// https://plugins.gradle.org/plugin/com.github.johnrengelman.shadow
	plugin(id = "com.github.johnrengelman.shadow", version = "8.1.1")
	// https://github.com/unimined/unimined/releases/latest
	plugin(id = "xyz.wagyourtail.unimined", version = "1.2.6")
	// https://plugins.gradle.org/plugin/com.github.gmazzo.buildconfig
	plugin(id = "com.github.gmazzo.buildconfig", version = "5.3.5")
	// https://github.com/ajoberstar/grgit/releases/latest
	plugin(id = "org.ajoberstar.grgit", version = "5.2.2")
	// https://plugins.gradle.org/plugin/me.modmuss50.mod-publish-plugin
	plugin(id = "me.modmuss50.mod-publish-plugin", version = "0.5.1")
}