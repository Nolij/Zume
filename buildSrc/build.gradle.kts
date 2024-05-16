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
	implementation("net.fabricmc:mapping-io:0.3.0")

	implementation("org.apache.ant:ant:1.10.13")
	implementation("com.guardsquare:proguard-base:7.4.2")
	plugin(id = "com.github.johnrengelman.shadow", version = "8.1.1")
	plugin(id = "xyz.wagyourtail.unimined", version = "1.2.4")
	plugin(id = "com.github.gmazzo.buildconfig", version = "5.2.0")
	plugin(id = "org.ajoberstar.grgit", version = "5.2.2")
	plugin(id = "me.modmuss50.mod-publish-plugin", version = "0.4.5")
}