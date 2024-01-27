import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import me.modmuss50.mpp.platforms.curseforge.CurseforgeApi
import okhttp3.internal.immutableListOf
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask

plugins {
    id("java")
	id("maven-publish")
	id("com.github.johnrengelman.shadow")
	id("me.modmuss50.mod-publish-plugin")
	id("xyz.wagyourtail.unimined")
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
	val implName = subProject.name
	
	apply(plugin = "xyz.wagyourtail.unimined")
	
	group = "maven_group"()
	version = "mod_version"()
	
	base {
		archivesName = "${"archives_base_name"()}-${subProject.name}"
	}
	
	tasks.withType<GenerateModuleMetadata> {
		enabled = false
	}

	if (implName in uniminedImpls) {
		apply(plugin = "com.github.johnrengelman.shadow")

		configurations {
			val shade = create("shade")

			compileClasspath.get().extendsFrom(shade)
			runtimeClasspath.get().extendsFrom(shade)
		}

		dependencies {
			"shade"("blue.endless:jankson:${"jankson_version"()}") { isTransitive = false }

			"shade"(project(":common")) { isTransitive = false }
		}
		
		afterEvaluate {
			val platformJar = tasks.create<ShadowJar>("platformJar") {
				from("../LICENSE") {
					rename { "${it}_${"archives_base_name"()}" }
				}

				val remapJar = tasks.withType<RemapJarTask>()["remapJar"]
				dependsOn(remapJar)
				from(remapJar)

				configurations = immutableListOf(project.configurations["shade"])
				archiveBaseName = rootProject.name
				archiveClassifier = implName
				isPreserveFileTimestamps = false

				relocate("blue.endless.jankson", "dev.nolij.zume.shadow.blue.endless.jankson")

				if (implName in lexForgeImpls) {
					manifest {
						attributes(
							"MixinConfigs" to "zume-${implName}.mixins.json",
						)
					}
					
					if (implName in legacyForgeImpls) {
						manifest {
							attributes(
								"ForceLoadAsMod" to true,
								"FMLCorePluginContainsFMLMod" to true,
								"TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
							)
						}
					}
				}
			}

			tasks.build {
				dependsOn(platformJar)
			}
		}
	} else {
		dependencies {
			implementation("blue.endless:jankson:${"jankson_version"()}")
		}
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

	inputs.file("gradle.properties")
	
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
			exclude("fabric.mod.json", "mcmod.info", "META-INF/mods.toml", "pack.mcmeta")
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
			"TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
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
		displayName = "mod_version"()
		version = "mod_version"()
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
			
			minecraftVersionRange {
				start = "1.7.10"
				end = "1.12.2"
				
				includeSnapshots = true
			}
			
			minecraftVersions.add("b1.7.3")
		}
		
		curseforge {
			val cfAccessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
			accessToken = cfAccessToken
			projectId = "927564"
			projectSlug = "zume"
			
			minecraftVersionRange {
				start = "1.14.4"
				end = "latest"
			}
	
			minecraftVersionRange {
				start = "1.7.10"
				end = "1.12.2"
			}

			if (cfAccessToken.orNull != null) {
				val cfAPI = CurseforgeApi(cfAccessToken.get(), apiEndpoint.get())
				
				val mcVersions = minecraftVersions.get().map {
					"${it}-Snapshot"
				}.toHashSet()
				
				@Suppress("UnstableApiUsage")
				minecraftVersions.addAll(providerFactory.provider {
					cfAPI.getGameVersions().map {
						it.name
					}.filter {
						it.endsWith("-Snapshot")
					}.filter { cfVersion ->
						mcVersions.contains(cfVersion)
					}.toHashSet()
				})
			}

			minecraftVersions.add("1.20.5-Snapshot")
			minecraftVersions.add("Beta 1.7.3")
		}
		
		discord {
			webhookUrl = providers.environmentVariable("DISCORD_WEBHOOK")
			
			username = "Zume Updates"
			
			avatarUrl = "https://github.com/Nolij/Zume/raw/master/common/src/main/resources/assets/zume/icon_large.png"
			
			content = changelog.map {
				"# A new version of Zume has been released! ```md\n${it}\n```"
			}
		}
	}
}