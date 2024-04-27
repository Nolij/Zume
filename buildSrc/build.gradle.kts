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
	implementation("org.ow2.asm:asm-tree:9.7")
	implementation("com.google.code.gson:gson:2.10.1")

	implementation("org.apache.ant:ant:1.10.13")
	plugin("com.github.johnrengelman.shadow", "8.1.1")
	plugin("xyz.wagyourtail.unimined", "1.2.3")
	plugin("com.github.gmazzo.buildconfig", "5.2.0")
	plugin("org.ajoberstar.grgit", "5.2.2")
	plugin("me.modmuss50.mod-publish-plugin", "0.4.5")
}