@file:Suppress("UnstableApiUsage")
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.nolij.zumegradle.MixinConfigMergingTransformer
import dev.nolij.zumegradle.entryprocessing.EntryProcessors
import dev.nolij.zumegradle.smoketest.SmokeTest.Config
import dev.nolij.zumegradle.task.SmokeTestTask
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.taumc.gradle.compression.DeflateAlgorithm
import org.taumc.gradle.compression.JsonShrinkingType
import org.taumc.gradle.compression.task.AdvzipTask
import org.taumc.gradle.compression.task.CopyJarTask
import org.taumc.gradle.compression.task.JarEntryModificationTask
import org.taumc.gradle.compression.task.ProguardTask
import org.taumc.gradle.minecraft.ModEnvironment
import org.taumc.gradle.minecraft.ModLoader
import org.taumc.gradle.publishing.api.PublishChannel
import ru.vyarus.gradle.plugin.python.PythonExtension
import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask
import xyz.wagyourtail.unimined.api.unimined
import kotlin.math.max

plugins {
    id("java")
	id("maven-publish")
	id("org.taumc.gradle.versioning")
	id("org.taumc.gradle.compression")
	id("org.taumc.gradle.publishing")
	id("com.gradleup.shadow")
	id("xyz.wagyourtail.jvmdowngrader")
	id("xyz.wagyourtail.unimined")
	id("ru.vyarus.use-python")
}

operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

Zume.auditAndExitEnabled = (rootProject.properties["withAuditAndExit"] as String?).toBoolean()

enum class BuildChannel(
	val deflation: DeflateAlgorithm = DeflateAlgorithm.INSANE,
	val json: JsonShrinkingType = JsonShrinkingType.MINIFY,
	val proguard: Boolean = true,
	) {
	DEV_BUILD(
		deflation = DeflateAlgorithm.EXTRA,
		json = JsonShrinkingType.PRETTY_PRINT
	),
	PRE_RELEASE,
	RELEASE_CANDIDATE,
	RELEASE,
}

Zume.version = tau.versioning.version("mod_version"(), project.properties["release_channel"])
println("Zume Version: ${tau.versioning.version}")

val buildChannel = BuildChannel.valueOf(tau.versioning.releaseChannel.name)

rootProject.group = "maven_group"()
rootProject.version = tau.versioning.version

base {
	archivesName = "mod_id"()
}

fun arrayOfProjects(vararg projectNames: String): Array<String> {
	return listOf(*projectNames).filter { p -> findProject(p) != null }.toTypedArray()
}

val fabricImpls = arrayOfProjects(
	"modern",
	"legacy",
	"primitive",
)
val legacyForgeImpls = arrayOfProjects(
	"vintage",
	"archaic",
)
val lexForgeImpls = arrayOfProjects(
	"lexforge",
	"lexforge18",
	"lexforge16",
)
val neoForgeImpls = arrayOfProjects(
	"neoforge",
)
val forgeImpls = arrayOf(
	*lexForgeImpls,
	*neoForgeImpls,
)
val uniminedImpls = arrayOf(
	*fabricImpls,
	*legacyForgeImpls,
	*lexForgeImpls,
	*neoForgeImpls,
)

allprojects {	
	apply(plugin = "java")
	apply(plugin = "idea")
	apply(plugin = "maven-publish")

	idea.module {
		isDownloadJavadoc = true
		isDownloadSources = true
	}

	repositories {
		maven("https://maven.wagyourtail.xyz/snapshots")
		maven("https://maven.wagyourtail.xyz/releases")
		maven("https://repo.spongepowered.org/maven")
		maven("https://jitpack.io/")
		exclusiveContent { 
			forRepository { maven("https://api.modrinth.com/maven") }
			filter { 
				includeGroup("maven.modrinth")
			}
		}
		exclusiveContent {
			forRepository { maven("https://cursemaven.com") }
			filter {
				includeGroup("curse.maven")
			}
		}
		maven("https://maven.blamejared.com")
		maven("https://maven.taumc.org/releases")
		mavenLocal()
	}
	
	tasks.withType<JavaCompile> {
		if (name !in arrayOf("compileMcLauncherJava", "compilePatchedMcJava")) {
			options.encoding = "UTF-8"
			javaCompiler = javaToolchains.compilerFor {
				languageVersion = JavaLanguageVersion.of(21)
			}
			
			options.compilerArgs.addAll(arrayOf("-Xplugin:Manifold no-bootstrap"))
		}
	}
	
	dependencies {
		compileOnly("org.jetbrains:annotations:${"jetbrains_annotations_version"()}")
		
		annotationProcessor("xyz.wagyourtail.jvmdowngrader:jvmdowngrader:${"jvmdg_version"()}:all")

		compileOnly("systems.manifold:manifold-rt:${"manifold_version"()}")
		annotationProcessor("systems.manifold:manifold-exceptions:${"manifold_version"()}")
	}

	tasks.processResources {
		inputs.file(rootDir.resolve("gradle.properties"))
		inputs.property("version", rootProject.tau.versioning.version)

		filteringCharset = "UTF-8"

		val props = mutableMapOf<String, String>()
		props.putAll(rootProject.properties
			.filterValues { value -> value is String }
			.mapValues { entry -> entry.value as String })
		props["mod_version"] = rootProject.tau.versioning.version

		filesMatching(listOf("fabric.mod.json", "mcmod.info", "META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
			expand(props)
		}
	}

	tasks.withType<AbstractArchiveTask>().configureEach {
		isPreserveFileTimestamps = false
		isReproducibleFileOrder = true
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		
		// TODO: yell at wyt to fix Unimined
		includeEmptyDirs = false
		exclude("assets/minecraft/textures/**")
	}
}

subprojects {
	val subProject = this
	val implName = subProject.name
	
	group = "maven_group"()
	version = rootProject.tau.versioning.version
	
	base {
		archivesName = "${"mod_id"()}-${subProject.name}"
	}
	
	tasks.withType<GenerateModuleMetadata> {
		enabled = false
	}

	if (implName in uniminedImpls) {
		apply(plugin = "xyz.wagyourtail.unimined")
		apply(plugin = "com.gradleup.shadow")
		
		unimined.footgunChecks = false
		
		unimined.minecraft(sourceSets["main"], lateApply = true) {
			combineWith(project(":api").sourceSets.main.get())

			if (implName != "primitive") {
				runs.config("server") {
					enabled = false
				}
				
				runs.config("client") {
					jvmArguments.add("-Dzume.configPathOverride=${rootProject.file("zume.json5").absolutePath}")
				}
			}
			
			defaultRemapJar = true
		}
		
		val outputJar by tasks.registering(ShadowJar::class) {
			group = "build"
			
			val remapJarTasks = tasks.withType<RemapJarTask>()
			dependsOn(remapJarTasks)
			mustRunAfter(remapJarTasks)
			remapJarTasks.forEach { remapJar ->
				remapJar.asJar.archiveFile.also { archiveFile ->
					from(zipTree(archiveFile))
					inputs.file(archiveFile)
				}
			}

			configurations = emptyList()
			archiveClassifier = "output"
			
			relocate("dev.nolij.zume.integration.implementation", "dev.nolij.zume.${implName}.integration")
		}

		tasks.assemble {
			dependsOn(outputJar)
		}
		
		tasks.withType<RemapJarTask> {
			mixinRemap {
				enableMixinExtra()
				disableRefmap()
			}
		}
	}
	
	if (implName in forgeImpls) {
		val minecraftLibraries by configurations.getting
		
		dependencies {
			minecraftLibraries("dev.nolij:zson:${"zson_version"()}:downgraded-8")
			minecraftLibraries("dev.nolij:libnolij:${"libnolij_version"()}:downgraded-8")
		}
	} else {
		dependencies {
			implementation("dev.nolij:zson:${"zson_version"()}:downgraded-8")
			implementation("dev.nolij:libnolij:${"libnolij_version"()}:downgraded-8")
		}
	}
}

unimined.footgunChecks = false

unimined.minecraft {
	version("modern_minecraft_version"())
	
	runs.off = true

	fabric {
		loader("fabric_version"())
	}

	mappings {
		mojmap()
	}

	defaultRemapJar = false
}

val shade: Configuration by configurations.creating {
	configurations.compileClasspath.get().extendsFrom(this)
	configurations.runtimeClasspath.get().extendsFrom(this)
}

dependencies {
	shade("dev.nolij:zson:${"zson_version"()}:downgraded-8")
	shade("dev.nolij:libnolij:${"libnolij_version"()}:downgraded-8")

	compileOnly("org.apache.logging.log4j:log4j-core:${"log4j_version"()}")
	
	compileOnly(project(":stubs"))
	
	implementation(project(":api"))
	
	uniminedImpls.forEach { 
		implementation(project(":${it}")) { isTransitive = false }
	}
}

tasks.jar {
	enabled = false
}

val sourcesJar by tasks.registering(Jar::class) {
	group = "build"

	archiveClassifier = "sources"
	
	from("LICENSE") {
		rename { "${it}_${"mod_id"()}" }
	}
	
	if (buildChannel.proguard) {
		dependsOn(proguardJar)
		from(proguardJar.get().mappingsFile) {
			rename { "mappings.txt" }
		}
	}
	
	listOf(
		sourceSets, 
		project(":api").sourceSets, 
		project(":integration:embeddium").sourceSets,
		uniminedImpls.flatMap { project(":${it}").sourceSets }
	).flatten().forEach {
		from(it.allSource) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
	}
}

tasks.shadowJar {
	transform(MixinConfigMergingTransformer::class.java) {
		modId = "mod_id"()
		packageName = "dev.nolij.zume.mixin"
		mixinPlugin = "dev.nolij.zume.ZumeMixinPlugin"
	}
	
	from("LICENSE") {
		rename { "${it}_${"mod_id"()}" }
	}
	
	exclude("*.xcf")
	exclude("LICENSE_zson")
	exclude("LICENSE_libnolij")
	
	configurations = listOf(shade)
	archiveClassifier = "deobfuscated"
	
	val apiJar = project(":api").tasks.jar
	dependsOn(apiJar)
	from(zipTree(apiJar.get().archiveFile.get())) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
	
	uniminedImpls.map { project(it).tasks.named<ShadowJar>("outputJar").get() }.forEach { implJarTask ->
		dependsOn(implJarTask)
		from(zipTree(implJarTask.archiveFile.get())) {
			duplicatesStrategy = DuplicatesStrategy.EXCLUDE
			exclude("fabric.mod.json", "mcmod.info", "META-INF/mods.toml", "pack.mcmeta")

			filesMatching(listOf(
				"dev/nolij/zume/lexforge/LexZume.class",
				"dev/nolij/zume/lexforge18/LexZume18.class",
				"dev/nolij/zume/lexforge16/LexZume16.class",
				"dev/nolij/zume/vintage/VintageZume.class")) {
				val reader = ClassReader(this.open())
				val node = ClassNode()
				reader.accept(node, 0)

				node.visibleAnnotations?.removeIf { it.desc == "Lnet/minecraftforge/fml/common/Mod;" }

				val writer = ClassWriter(0)
				node.accept(writer)
				this.file.writeBytes(writer.toByteArray())
			}
		}
	}
	
	relocate("dev.nolij.zson", "dev.nolij.zume.zson")
	relocate("dev.nolij.libnolij", "dev.nolij.zume.libnolij")
	if (buildChannel.proguard) {
		relocate("dev.nolij.zume.mixin", "zume.mixin")
	}
	
	manifest {
		attributes(
			"FMLCorePluginContainsFMLMod" to true,
			"ForceLoadAsMod" to true,
			"MixinConfigs" to "zume.mixins.json",
			"TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
			"Fabric-Loom-Mixin-Remap-Type" to "static",
		)
	}
}

jvmdg.defaultShadeTask {
	enabled = false
}

jvmdg.defaultTask {
	dependsOn(tasks.shadowJar)
	
	inputFile = provider { tasks.shadowJar.get().archiveFile.get() }
	downgradeTo = JavaVersion.VERSION_1_8
	
	// jvmdg stubs System.getProperty("native.encoding") but we don't use it so it's fine
	debugSkipStub.add("Lxyz/wagyourtail/jvmdg/j18/stub/java_base/J_L_System;")
	debugSkipStub.add("Lxyz/wagyourtail/jvmdg/j18/stub/java_base/J_L_System;getProperty;(Ljava/lang/String;)Ljava/lang/String;")
}

// TODO: fix
//tasks.register<JvmdgStubCheckTask>("jvmdgCheck${this.name.capitalized()}") {
//	classesRoot(compile.destinationDirectory)
//	this.mustRunAfter(compile)
//	compile.finalizedBy(this)
//
//	allowedStubs.addAll(
//		"xyz/wagyourtail/jvmdg/j21/stub/java_base/J_L_MatchException", // we remove manually later
//	)
//}

//region compressJar
val cjTempDir = layout.buildDirectory.dir("compressJar")
val proguardJar by tasks.registering(ProguardTask::class) {
	dependsOn(jvmdg.defaultTask)
	inputJar = jvmdg.defaultTask.get().archiveFile
	destinationDirectory = cjTempDir
	run = buildChannel.proguard
	
	config(file("proguard.pro"))
	
	mappingsFile = destinationDirectory.get().asFile
		.resolve("${archiveFile.get().asFile.nameWithoutExtension}-mappings.txt")

	jmod("java.base")
	jmod("java.desktop")

	classpath.from(
		uniminedImpls.flatMap { implName -> project(":$implName").unimined.minecrafts.values }.flatMap { minecraftConfig ->
			val prodNamespace = minecraftConfig.mcPatcher.prodNamespace

			val minecrafts = listOf(
				minecraftConfig.sourceSet.compileClasspath.files,
				minecraftConfig.sourceSet.runtimeClasspath.files
			)
				.flatten()
				.filter { !minecraftConfig.isMinecraftJar(it.toPath()) }
				.toHashSet()

			return@flatMap minecraftConfig.mods.getClasspathAs(prodNamespace, prodNamespace, minecrafts)
				.filter { it.extension == "jar" && !it.name.startsWith("zume") }
				.plus(minecraftConfig.getMinecraft(prodNamespace, prodNamespace).toFile())
		}
	)
	
	archiveClassifier = "proguard"
}

subprojects {
	val subProject = this
	val implName = subProject.name

	if (implName in uniminedImpls) {
		proguardJar {
			classpath.from(
				provider {
					subProject.unimined.minecrafts.values
						.flatMap { minecraftConfig ->
							val prodNamespace = minecraftConfig.mcPatcher.prodNamespace

							val minecrafts = listOf(
								minecraftConfig.sourceSet.compileClasspath.files,
								minecraftConfig.sourceSet.runtimeClasspath.files
							)
								.flatten()
								.filter { !minecraftConfig.isMinecraftJar(it.toPath()) }
								.toHashSet()

							return@flatMap minecraftConfig.mods
								.getClasspathAs(prodNamespace, prodNamespace, minecrafts)
								.filter { it.extension == "jar" && !it.name.startsWith("zume") }
								.plus(minecraftConfig.getMinecraft(prodNamespace, prodNamespace).toFile())
						}
				}
			)
		}
	}
}

val minifyJar by tasks.registering(JarEntryModificationTask::class) {
	dependsOn(proguardJar)
	inputJar = proguardJar.get().archiveFile
	destinationDirectory = cjTempDir

	archiveClassifier = "minified"
	json(buildChannel.json) {
		it.endsWith(".json") || it.endsWith(".mcmeta") || it == "mcmod.info"
	}
	
	process(EntryProcessors.modifyClass { 
		val matchExceptions = setOf(
			"xyz/wagyourtail/jvmdg/j21/stub/java_base/J_L_MatchException",
			"java/lang/MatchException"
		)
		
		it.methods.forEach { 
			it.instructions?.forEach { instruction ->
				
				// creating the object
				if (instruction.opcode == Opcodes.NEW) {
					instruction as org.objectweb.asm.tree.TypeInsnNode
					if(instruction.desc in matchExceptions) {
						instruction.desc = "java/lang/IllegalStateException"
					}
				}
				
				// calling <init>
				if(instruction.opcode == Opcodes.INVOKESPECIAL) {
					instruction as org.objectweb.asm.tree.MethodInsnNode
					if(instruction.owner in matchExceptions && instruction.name == "<init>") {
						instruction.owner = "java/lang/IllegalStateException"
					}
				}
			}
		}
	})

	process(EntryProcessors.removeAnnotations {
		it.desc.startsWith("Ldev/nolij/zumegradle/proguard/")
	})

	if (buildChannel.proguard) {
		process(EntryProcessors.obfuscationFixer(proguardJar.get().mappingsFile.get().asFile))
	}
}

val advzip by tasks.registering(AdvzipTask::class) {
	dependsOn(minifyJar)
	inputJar = minifyJar.get().archiveFile
	destinationDirectory = cjTempDir
	
	archiveClassifier = "advzip"
	level = buildChannel.deflation
}

val compressJar by tasks.registering(CopyJarTask::class) {
	dependsOn(advzip)
	group = "build"
	inputJar = advzip.get().archiveFile
	
	archiveClassifier = null
}
//endregion

tasks.assemble {
	dependsOn(compressJar, sourcesJar)
}

//region Smoke Testing
python {
	scope = PythonExtension.Scope.VIRTUALENV
	envPath = "${project.rootDir}/.gradle/python"
	pip("portablemc:${"portablemc_version"()}")
	pip("certifi:2025.11.12")
}

val smokeTest by tasks.registering(SmokeTestTask::class) {
	dependsOn(tasks.checkPython, tasks.pipInstall, compressJar)
	
	inputTask = compressJar
	portableMCBinary = "${python.envPath}/bin/portablemc"
	mainDir = "${project.rootDir}/.gradle/portablemc"
	workDir = "${project.layout.buildDirectory.get()}/smoke_test"
	maxThreads = max(2, Runtime.getRuntime().availableProcessors() / 5)
	threadTimeout = TimeUnit.SECONDS.toNanos(60)

	configs(
//		Config("fabric", "snapshot", dependencies = setOf(
//			"maven.modrinth:fabric-api:+",
//		)),
		Config("fabric", "1.21.8", dependencies = setOf(
			"maven.modrinth:fabric-api:0.130.0+1.21.8",
			"maven.modrinth:modmenu:15.0.0-beta.3",
		)),
		Config("fabric", "1.21.1", dependencies = setOf(
			"maven.modrinth:fabric-api:0.107.0+1.21.1",
			"maven.modrinth:modmenu:11.0.3",
		)),
		Config("fabric", "1.20.6", dependencies = setOf(
			"maven.modrinth:fabric-api:0.100.8+1.20.6",
			"maven.modrinth:modmenu:10.0.0",
		)),
		Config("fabric", "1.20.1", dependencies = setOf(
			"maven.modrinth:fabric-api:0.92.2+1.20.1",
			"maven.modrinth:modmenu:7.2.2",
		)),
		Config("fabric", "1.18.2", dependencies = setOf(
			"maven.modrinth:fabric-api:0.77.0+1.18.2",
			"maven.modrinth:modmenu:3.2.5",
			"maven.modrinth:lazydfu:0.1.2",
		), extraArgs = listOf("--lwjgl=3.2.3")),
		Config("fabric", "1.16.5", dependencies = setOf(
			"maven.modrinth:fabric-api:0.42.0+1.16",
			"maven.modrinth:modmenu:1.16.23",
			"maven.modrinth:lazydfu:0.1.2",
		)),
		Config("fabric", "1.14.4", dependencies = setOf(
			"maven.modrinth:fabric-api:0.28.5+1.14",
			"maven.modrinth:modmenu:1.7.17",
			"maven.modrinth:lazydfu:0.1.2",
		)),
		Config("legacyfabric", "1.12.2", dependencies = setOf(
			"maven.modrinth:legacy-fabric-api:1.10.2",
		)),
		Config("legacyfabric", "1.8.9", dependencies = setOf(
			"maven.modrinth:legacy-fabric-api:1.10.2",
		)),
		Config("legacyfabric", "1.7.10", dependencies = setOf(
			"maven.modrinth:legacy-fabric-api:1.10.2",
		)),
//		Config("legacyfabric", "1.6.4", dependencies = setOf(
//			"maven.modrinth:legacy-fabric-api:1.10.2",
//		)),
		Config("babric", "b1.7.3", jvmVersion = 17, dependencies = setOf(
			"maven.modrinth:stationapi:2.0-alpha.2.4",
		), extraArgs = listOf("--exclude-lib=asm-all")),
		Config("neoforge", "release"),
		Config("neoforge", "1.21.1"),
		Config("neoforge", "1.20.4"),
		Config("forge", "1.20.4"),
		Config("forge", "1.20.1"),
		Config("forge", "1.19.2", dependencies = setOf(
			"curse.maven:lazydfu-460819:4327266"
		)),
		Config("forge", "1.18.2", dependencies = setOf(
			"curse.maven:lazuyfu-460819:3544496"
		), extraArgs = listOf("--lwjgl=3.2.3")),
		Config("forge", "1.16.5", dependencies = setOf(
			"curse.maven:lazydfu-460819:3249059"
		), extraArgs = listOf("--lwjgl=3.2.3")),
		Config("forge", "1.14.4", dependencies = setOf(
			"maven.modrinth:mixinbootstrap:1.1.0"
		), extraArgs = listOf("--lwjgl=3.2.3")),
		Config("forge", "1.12.2", dependencies = setOf(
			"maven.modrinth:mixinbooter:9.3"
		)),
		Config("forge", "1.8.9", dependencies = setOf(
			"maven.modrinth:mixinbooter:9.3"
		)),
		Config("forge", "1.7.10", dependencies = setOf(
			"maven.modrinth:unimixins:1.7.10-0.1.19"
		)),
	)
}
//endregion

publishing {
	repositories {
		if (!System.getenv("local_maven_url").isNullOrEmpty())
			maven(System.getenv("local_maven_url"))
	}

	publications {
		create<MavenPublication>("mod_id"()) {
			artifact(provider { compressJar.get().archiveFile })
			artifact(provider { sourcesJar.get().archiveFile })
		}
	}
}

tau.publishing.publish {
	dependsOn(compressJar, sourcesJar)
	
	useTauGradleVersioning()
	changelog = file("CHANGELOG.md").readText()
	
	modArtifact { 
		files(provider { compressJar.get().archiveFile }, provider { sourcesJar.get().archiveFile })
		
		version = tau.versioning.version
		
		minecraftVersionRange = "minecraft_version_range"()
		supportsSnapshots = true
		javaVersions.addAll(JavaVersion.values().filter { it.ordinal >= JavaVersion.VERSION_1_8.ordinal })
		
		environment = ModEnvironment.CLIENT_ONLY
		modLoaders.addAll(ModLoader.FABRIC, ModLoader.LEXFORGE, ModLoader.NEOFORGE)
	}
	
	github { 
		supportAllChannels()
		
		accessToken = providers.environmentVariable("GITHUB_TOKEN")
		repository = "Nolij/Zume"
		tagName = tau.versioning.releaseTag
	}
	
	modrinth {
		supportChannels(PublishChannel.RELEASE)
		
		accessToken = providers.environmentVariable("MODRINTH_TOKEN")
		projectID = "o6qsdrrQ"
		projectSlug = "zume"
	}
	
	curseforge {
		supportChannels(PublishChannel.RELEASE)
		
		accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
		projectID = 927564
		projectSlug = "zume"
	}
	
	val iconURL = "https://github.com/Nolij/Zume/raw/master/icon_padded_large.png"
	
	discord {
		supportAllChannelsExcluding(PublishChannel.RELEASE)
		
		webhookURL = providers.environmentVariable("DISCORD_WEBHOOK")
		avatarURL = iconURL

		testBuildPreset(modName = "Zume", repoURL = "https://github.com/Nolij/Zume")
	}
	
	discord {
		supportChannels(PublishChannel.RELEASE)
		
		webhookURL = providers.environmentVariable("DISCORD_WEBHOOK")
		avatarURL = iconURL
		
		releasePreset(modName = "Zume")
	}
}