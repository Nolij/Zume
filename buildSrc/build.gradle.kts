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
	implementation("com.guardsquare:proguard-gradle:7.4.2")

	implementation("org.apache.ant:ant:1.10.13")
	plugin(id = "com.github.johnrengelman.shadow", version = "8.1.1")
	plugin(id = "xyz.wagyourtail.unimined", version = "1.2.3")
	plugin(id = "com.github.gmazzo.buildconfig", version = "5.2.0")
	plugin(id = "org.ajoberstar.grgit", version = "5.2.2")
	plugin(id = "me.modmuss50.mod-publish-plugin", version = "0.4.5")
}

gradlePlugin {
	plugins {
		create("proguard") {
			id = "dev.nolij.zume-proguard"
			implementationClass = "dev.nolij.zumegradle.ZumeProGuard"
		}
	}
}