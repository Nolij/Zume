import okhttp3.internal.immutableListOf
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask

buildscript {
	dependencies {
		classpath("org.ow2.asm:asm-tree:9.6")
	}
}

plugins {
    id("java")
	id("com.github.johnrengelman.shadow") version "8.1.1"
    id("maven-publish")
	id("me.modmuss50.mod-publish-plugin") version "0.4.5"
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

val fabricImpls = arrayOf(
	"modern",
	"legacy",
	"primitive",
)
val legacyForgeImpls = arrayOf(
	"vintage",
	"archaic",
)
val lexForgeImpls = arrayOf(
	"lexforge",
	"lexforge18",
	"lexforge16",
	*legacyForgeImpls,
)
val neoForgeImpls = arrayOf(
	"neoforge",
)
val uniminedImpls = arrayOf(
	*fabricImpls,
	*lexForgeImpls,
	*neoForgeImpls,
)
val impls = arrayOf(
	"common",
	*uniminedImpls,
)

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

configurations {
	val shade = create("shade")
	
	compileClasspath.get().extendsFrom(shade)
	runtimeClasspath.get().extendsFrom(shade)
}

dependencies {
	"shade"("blue.endless:jankson:${"jankson_version"()}")

	// https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core
	compileOnly("org.apache.logging.log4j:log4j-core:2.22.0")
	
	compileOnly(project(":stubs"))
	
	"shade"(project(":common")) { isTransitive = false }
	
	uniminedImpls.forEach { 
		implementation(project(":${it}")) { isTransitive = false }
	}
}

tasks.processResources {
	from("common/src/main/resources")

	filteringCharset = "UTF-8"

	filesMatching(immutableListOf("fabric.mod.json", "mcmod.info", "META-INF/mods.toml")) {
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
			exclude("fabric.mod.json", "mcmod.info", "META-INF/mods.toml")
		}
	}
	
	filesMatching(immutableListOf(
			"dev/nolij/zume/lexforge/LexZume.class", 
			"dev/nolij/zume/lexforge18/LexZume18.class", 
			"dev/nolij/zume/lexforge16/LexZume16.class", 
			"dev/nolij/zume/vintage/VintageZume.class")) {
		val reader = ClassReader(this.open())
		val node = ClassNode()
		reader.accept(node, 0)
		
		node.visibleAnnotations.removeIf { it.desc == "Lnet/minecraftforge/fml/common/Mod;" }
		
		val writer = ClassWriter(0)
		node.accept(writer)
		this.file.writeBytes(writer.toByteArray())
	}
	
	relocate("blue.endless.jankson", "dev.nolij.zume.shadow.blue.endless.jankson")
	
	manifest {
		attributes(
			"FMLCorePluginContainsFMLMod" to true,
			"ForceLoadAsMod" to true,
			"MixinConfigs" to lexForgeImpls.joinToString(",") { "zume-${it}.mixins.json" },
			"TweakClass" to "org.spongepowered.asm.launch.MixinTweaker"
		)
	}
}

tasks.build {
	dependsOn(tasks.shadowJar)
}

afterEvaluate {
	publishMods {
		file = tasks.shadowJar.get().archiveFile
		type = STABLE
		displayName = "mod_name"()
		changelog = file("CHANGELOG.md").readText()
		
		modLoaders.addAll("fabric", "forge", "neoforge")
		dryRun = providers.environmentVariable("GITHUB_TOKEN").getOrNull() == null
		
		github {
			accessToken = providers.environmentVariable("GITHUB_TOKEN")
			repository = "Nolij/Zume"
			commitish = "master"
			tagName = "release/${"mod_version"()}"
		}
		
		modrinth {
			accessToken = providers.environmentVariable("MODRINTH_TOKEN")
			projectId = "o6qsdrrQ"
			
			minecraftVersionRange {
				start = "1.14.4"
				end = "latest"
				
				includeSnapshots = true
			}
			
			minecraftVersions.add("b1.7.3")
			minecraftVersions.add("1.7.10")
			minecraftVersions.add("1.8.9")
			minecraftVersions.add("1.9.4")
			minecraftVersions.add("1.10.2")
			minecraftVersions.add("1.11.2")
			minecraftVersions.add("1.12.2")
		}
		
		curseforge {
			accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
			projectId = "927564"
			
			minecraftVersionRange {
				start = "1.14.4"
				end = "latest"
			}
	
			minecraftVersions.add("b1.7.3")
			minecraftVersions.add("1.7.10")
			minecraftVersions.add("1.8.9")
			minecraftVersions.add("1.9.4")
			minecraftVersions.add("1.10.2")
			minecraftVersions.add("1.11.2")
			minecraftVersions.add("1.12.2")
		}
	}
}