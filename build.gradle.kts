import okhttp3.internal.immutableListOf
import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask

plugins {
    id("java")
	id("com.github.johnrengelman.shadow") version "8.1.1"
    id("maven-publish")
	id("com.modrinth.minotaur") version "2.+"
	id("io.github.themrmilchmann.curseforge-publish") version "0.4.0"
	id("com.github.gmazzo.buildconfig") version "5.2.0" apply(false)
	id("xyz.wagyourtail.unimined") version "1.2.0-SNAPSHOT"
}

operator fun String.invoke(): String {
	return (rootProject.properties[this] as String?)!!
}

group = "maven_group"()
version = "mod_version"()

base {
	archivesName = "archives_base_name"()
}

allprojects {	
	apply(plugin = "java")
	apply(plugin = "maven-publish")

	repositories {
		mavenCentral()
		maven("https://repo.spongepowered.org/maven")
		maven("https://jitpack.io/")
		exclusiveContent {
			forRepository {
				maven("https://api.modrinth.com/maven")
			}
			filter {
				includeGroup("maven.modrinth")
			}
		}
	}
	
	tasks.withType<JavaCompile> {
		if (name !in arrayOf("compileMcLauncherJava", "compilePatchedMcJava")) {
			options.encoding = "UTF-8"
			sourceCompatibility = "17"
			options.release = 8
			javaCompiler = javaToolchains.compilerFor {
				languageVersion = JavaLanguageVersion.of(17)
			}
		}
	}
	
	dependencies {
		val jabelDependency = "com.github.bsideup.jabel:jabel-javac-plugin:${"jabel_version"()}"
		annotationProcessor(jabelDependency)
		compileOnly(jabelDependency)
	}
}

subprojects {
	val subProject = this
	
	apply(plugin = "xyz.wagyourtail.unimined")
	
	group = "maven_group"()
	version = "mod_version"()
	
	base {
		archivesName = "${"archives_base_name"()}-${subProject.name}"
	}
	
	tasks.withType<GenerateModuleMetadata> {
		enabled = false
	}
	
	dependencies {
		implementation("blue.endless:jankson:${"jankson_version"()}")
	}
}

unimined.minecraft {
	version("modern_minecraft_version"())
	
	runs {
		config("client") {
			disabled = true
		}
		config("server") {
			disabled = true
		}
	}

	fabric {
		loader("fabric_version"())
	}

	mappings {
		intermediary()
		yarn("modern_mappings_version"())
		devFallbackNamespace("intermediary")
	}

	defaultRemapJar = false
}

val fabricImpls = arrayOf(
	"modern",
	"legacy",
	"primitive",
)
val legacyForgeImpls = arrayOf(
	"archaic",
	"vintage",
)
val uniminedImpls = arrayOf(
	*fabricImpls,
	*legacyForgeImpls,
)
val impls = arrayOf(
	"common",
	*uniminedImpls,
)

configurations {
	val shade = create("shade")
	
	compileClasspath.get().extendsFrom(shade)
	runtimeClasspath.get().extendsFrom(shade)
}

dependencies {
	"shade"("blue.endless:jankson:${"jankson_version"()}")

	// https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core
	compileOnly("org.apache.logging.log4j:log4j-core:2.22.0")
	
	"shade"(project(":common")) { isTransitive = false }
	
	uniminedImpls.forEach { 
		implementation(project(":${it}")) { isTransitive = false }
	}
}

tasks.processResources {
	from("common/src/main/resources")

	filteringCharset = "UTF-8"

	filesMatching(immutableListOf("fabric.mod.json", "mcmod.info")) {
		expand(rootProject.properties)
	}
}

tasks.jar {
	enabled = false
}

tasks.shadowJar {
	val shadowJar = this
	from("LICENSE") {
		rename { "${it}_${"archives_base_name"()}" }
	}
	
	configurations = immutableListOf(project.configurations["shade"])
	archiveClassifier = ""
	isPreserveFileTimestamps = false
	
	uniminedImpls.forEach { 
		val remapJar = project(":${it}").tasks.withType<RemapJarTask>()["remapJar"]
		shadowJar.dependsOn(remapJar)
		from(zipTree(remapJar.archiveFile.get())) {
			exclude("fabric.mod.json", "mcmod.info")
		}
	}
	
	relocate("blue.endless.jankson", "dev.nolij.zume.shadow.blue.endless.jankson")
	
	manifest {
		attributes(
			"FMLCorePluginContainsFMLMod" to true,
			"ForceLoadAsMod" to true,
			"MixinConfigs" to legacyForgeImpls.joinToString(",") { "zume-${it}.mixins.json" },
			"TweakClass" to "org.spongepowered.asm.launch.MixinTweaker"
		)
	}
}

tasks.build {
	dependsOn(tasks.shadowJar)
}