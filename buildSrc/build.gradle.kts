import java.util.*

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

val properties = Properties().apply {
	load(rootDir.parentFile.resolve("gradle.properties").inputStream())
}

operator fun String.invoke(): String = properties.getProperty(this) ?: error("Property $this not found")

dependencies {
	implementation("org.ow2.asm:asm-tree:${"asm_version"()}")
	implementation("net.fabricmc:mapping-io:${"mapping_io_version"()}")
	implementation("org.apache.ant:ant:${"shadow_ant_version"()}")
	implementation("com.guardsquare:proguard-base:${"proguard_version"()}")
	
	plugin(id = "com.github.johnrengelman.shadow", version = "shadow_version"())
	plugin(id = "xyz.wagyourtail.unimined", version = "unimined_version"())
	plugin(id = "com.github.gmazzo.buildconfig", version = "buildconfig_version"())
	plugin(id = "org.ajoberstar.grgit", version = "grgit_version"())
	plugin(id = "me.modmuss50.mod-publish-plugin", version = "mod_publish_version"())
}