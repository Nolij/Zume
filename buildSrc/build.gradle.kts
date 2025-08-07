import java.util.Properties

plugins {
	id("idea")
	`kotlin-dsl`
}

idea.module {
	isDownloadJavadoc = true
	isDownloadSources = true
}

repositories {
	mavenCentral()
	maven("https://maven.wagyourtail.xyz/releases")
	maven("https://maven.wagyourtail.xyz/snapshots")
	gradlePluginPortal {
		content {
			excludeGroup("org.apache.logging.log4j")
		}
	}
}

kotlin {
	jvmToolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

fun DependencyHandler.plugin(id: String, version: String) = 
	this.implementation(group = id, name = "$id.gradle.plugin", version = version)

val gradleProperties = Properties().apply {
	load(rootDir.parentFile.resolve("gradle.properties").inputStream())
}

operator fun String.invoke(): String = gradleProperties.getProperty(this) ?: error("Property $this not found")

dependencies {
	implementation("org.ow2.asm:asm-tree:${"asm_version"()}")
	implementation("net.fabricmc:mapping-io:${"mapping_io_version"()}")
	implementation("org.apache.ant:ant:${"shadow_ant_version"()}")
	implementation("com.guardsquare:proguard-base:${"proguard_version"()}")
	
	plugin(id = "com.gradleup.shadow", version = "shadow_version"())
	plugin(id = "xyz.wagyourtail.unimined", version = "unimined_version"())
}