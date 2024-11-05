@file:Suppress("UnstableApiUsage")
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.nolij.zumegradle.DeflateAlgorithm
import dev.nolij.zumegradle.JsonShrinkingType
import dev.nolij.zumegradle.MixinConfigMergingTransformer
import dev.nolij.zumegradle.CompressJarTask
import kotlinx.serialization.encodeToString
import me.modmuss50.mpp.HttpUtils
import me.modmuss50.mpp.PublishModTask
import me.modmuss50.mpp.ReleaseType
import me.modmuss50.mpp.platforms.curseforge.CurseforgeApi
import me.modmuss50.mpp.platforms.discord.DiscordAPI
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.internal.immutableListOf
import okhttp3.internal.toHexString
import org.ajoberstar.grgit.Tag
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import ru.vyarus.gradle.plugin.python.PythonExtension
import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask
import xyz.wagyourtail.unimined.api.unimined
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Files
import java.time.ZonedDateTime
import kotlin.io.path.*
import kotlin.io.path.Path
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists

plugins {
    id("java")
	id("maven-publish")
	id("com.github.johnrengelman.shadow")
	id("me.modmuss50.mod-publish-plugin")
	id("xyz.wagyourtail.unimined")
	id("org.ajoberstar.grgit")
	id("ru.vyarus.use-python")
}

operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

enum class ReleaseChannel(
    val suffix: String? = null,
    val releaseType: ReleaseType? = null,
    val deflation: DeflateAlgorithm = DeflateAlgorithm.ZOPFLI,
    val json: JsonShrinkingType = JsonShrinkingType.MINIFY,
    val proguard: Boolean = true,
	) {
	DEV_BUILD(
		suffix = "dev",
		deflation = DeflateAlgorithm.SEVENZIP,
		json = JsonShrinkingType.PRETTY_PRINT
	),
	PRE_RELEASE("pre"),
	RELEASE_CANDIDATE("rc"),
	RELEASE(releaseType = ReleaseType.STABLE),
}

//region Git Versioning

val headDateTime: ZonedDateTime = grgit.head().dateTime

val branchName = grgit.branch.current().name!!
val releaseTagPrefix = "release/"

val releaseTags = grgit.tag.list()
	.filter { tag -> tag.name.startsWith(releaseTagPrefix) }
	.sortedWith { tag1, tag2 -> 
		if (tag1.commit.dateTime == tag2.commit.dateTime)
			if (tag1.name.length != tag2.name.length)
				return@sortedWith tag1.name.length.compareTo(tag2.name.length)
			else
				return@sortedWith tag1.name.compareTo(tag2.name)
		else
			return@sortedWith tag2.commit.dateTime.compareTo(tag1.commit.dateTime)
	}
	.dropWhile { tag -> tag.commit.dateTime > headDateTime }

val isExternalCI = (rootProject.properties["external_publish"] as String?).toBoolean()
val isRelease = rootProject.hasProperty("release_channel") || isExternalCI
val releaseIncrement = if (isExternalCI) 0 else 1
val releaseChannel: ReleaseChannel = 
	if (isExternalCI) {
		val tagName = releaseTags.first().name
		val suffix = """\-(\w+)\.\d+$""".toRegex().find(tagName)?.groupValues?.get(1)
		if (suffix != null)
			ReleaseChannel.values().find { channel -> channel.suffix == suffix }!!
		else
			ReleaseChannel.RELEASE
	} else {
		if (isRelease)
			ReleaseChannel.valueOf("release_channel"())
		else
			ReleaseChannel.DEV_BUILD
	}

println("Release Channel: $releaseChannel")

val minorVersion = "mod_version"()
val minorTagPrefix = "${releaseTagPrefix}${minorVersion}."

val patchHistory = releaseTags
	.map { tag -> tag.name }
	.filter { name -> name.startsWith(minorTagPrefix) }
	.map { name -> name.substring(minorTagPrefix.length) }

val maxPatch = patchHistory.maxOfOrNull { it.substringBefore('-').toInt() }
val patch =
	maxPatch?.plus(
		if (patchHistory.contains(maxPatch.toString()))
			releaseIncrement
		else
			0
	) ?: 0
var patchAndSuffix = patch.toString()

if (releaseChannel.suffix != null) {
	patchAndSuffix += "-${releaseChannel.suffix}"
	
	if (isRelease) {
		patchAndSuffix += "."
		
		val maxBuild = patchHistory
			.filter { it.startsWith(patchAndSuffix) }
			.mapNotNull { it.substring(patchAndSuffix.length).toIntOrNull() }
			.maxOrNull()
		
		val build = (maxBuild?.plus(releaseIncrement)) ?: 1
		patchAndSuffix += build.toString()
	}
}

//endregion

Zume.version = "${minorVersion}.${patchAndSuffix}"
println("Zume Version: ${Zume.version}")

rootProject.group = "maven_group"()
rootProject.version = Zume.version

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
	apply(plugin = "maven-publish")

	repositories {
		maven("https://repo.spongepowered.org/maven")
		maven("https://jitpack.io/")
		exclusiveContent { 
			forRepository { maven("https://api.modrinth.com/maven") }
			filter { 
				includeGroup("maven.modrinth")
			}
		}
		maven("https://maven.blamejared.com")
		maven("https://maven.taumc.org/releases")
	}
	
	tasks.withType<JavaCompile> {
		if (name !in arrayOf("compileMcLauncherJava", "compilePatchedMcJava")) {
			options.encoding = "UTF-8"
			sourceCompatibility = "21"
			options.release = 8
			javaCompiler = javaToolchains.compilerFor {
				languageVersion = JavaLanguageVersion.of(21)
			}
			options.compilerArgs.addAll(arrayOf("-Xplugin:Manifold no-bootstrap", "-Xplugin:jabel"))
			options.forkOptions.jvmArgs?.add("-XX:+EnableDynamicAgentLoading")
		}
	}
	
	dependencies {
		compileOnly("org.jetbrains:annotations:${"jetbrains_annotations_version"()}")
		
		annotationProcessor("com.pkware.jabel:jabel-javac-plugin:${"jabel_version"()}")

		compileOnly("systems.manifold:manifold-rt:${"manifold_version"()}")
		annotationProcessor("systems.manifold:manifold-exceptions:${"manifold_version"()}")
	}

	tasks.processResources {
		inputs.file(rootDir.resolve("gradle.properties"))
		inputs.property("version", Zume.version)

		filteringCharset = "UTF-8"

		val props = mutableMapOf<String, String>()
		props.putAll(rootProject.properties
			.filterValues { value -> value is String }
			.mapValues { entry -> entry.value as String })
		props["mod_version"] = Zume.version

		filesMatching(immutableListOf("fabric.mod.json", "mcmod.info", "META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
			expand(props)
		}
	}
}

subprojects {
	val subProject = this
	val implName = subProject.name
	
	group = "maven_group"()
	version = Zume.version
	
	base {
		archivesName = "${"mod_id"()}-${subProject.name}"
	}
	
	tasks.withType<GenerateModuleMetadata> {
		enabled = false
	}

	if (implName in uniminedImpls) {
		apply(plugin = "xyz.wagyourtail.unimined")
		apply(plugin = "com.github.johnrengelman.shadow")
		
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
		
		val outputJar = tasks.register<ShadowJar>("outputJar") {
			group = "build"
			
			val remapJarTasks = tasks.withType<RemapJarTask>()
			dependsOn(remapJarTasks)
			mustRunAfter(remapJarTasks)
			remapJarTasks.forEach { remapJar ->
				remapJar.archiveFile.also { archiveFile ->
					from(zipTree(archiveFile))
					inputs.file(archiveFile)
				}
			}

			configurations = emptyList()
			archiveClassifier = "output"
			isPreserveFileTimestamps = false
			isReproducibleFileOrder = true
			
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

val sourcesJar = tasks.register<Jar>("sourcesJar") {
	group = "build"

	archiveClassifier = "sources"
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
	
	from("LICENSE") {
		rename { "${it}_${"mod_id"()}" }
	}
	
	if (releaseChannel.proguard) {
		dependsOn(compressJar)
		from(compressJar.get().mappingsFile!!) {
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
	
	configurations = immutableListOf(shade)
	archiveClassifier = "deobfuscated"
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
	
	val apiJar = project(":api").tasks.jar
	dependsOn(apiJar)
	from(zipTree(apiJar.get().archiveFile.get())) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
	
	uniminedImpls.map { project(it).tasks.named<ShadowJar>("outputJar").get() }.forEach { implJarTask ->
		dependsOn(implJarTask)
		from(zipTree(implJarTask.archiveFile.get())) {
			duplicatesStrategy = DuplicatesStrategy.EXCLUDE
			exclude("fabric.mod.json", "mcmod.info", "META-INF/mods.toml", "pack.mcmeta")

			filesMatching(immutableListOf(
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
	if (releaseChannel.proguard) {
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

val compressJar = tasks.register<CompressJarTask>("compressJar") {
	dependsOn(tasks.shadowJar)
	group = "build"
	
	val shadowJar = tasks.shadowJar.get()
	inputJar = shadowJar.archiveFile.get().asFile
	outputJar = shadowJar.archiveFile.get().asFile.let { 
		it.parentFile.resolve("${it.nameWithoutExtension.removeSuffix("-deobfuscated")}.jar")
	}
	
	deflateAlgorithm = releaseChannel.deflation
	jsonShrinkingType = releaseChannel.json
	if (releaseChannel.proguard) {
		useProguard(uniminedImpls.flatMap { implName -> project(":$implName").unimined.minecrafts.values })
	}
}

tasks.assemble {
	dependsOn(compressJar, sourcesJar)
}

//region Smoke Testing
python {
	scope = PythonExtension.Scope.VIRTUALENV
	envPath = "${project.rootDir}/.gradle/python"
	pip("portablemc:${"portablemc_version"()}")
}

data class SmokeTestConfig(
	val modLoader: String,
	val mcVersion: String,
	val loaderVersion: String? = null,
	val jvmVersion: Int? = null,
	val extraArgs: List<String>? = null,
	val dependencies: List<Pair<String, String>>? = null,
) {	
	val versionString: String get() = 
		if (loaderVersion != null)
			"${modLoader}:${mcVersion}:${loaderVersion}"
		else
			"${modLoader}:${mcVersion}"
	
	override fun toString(): String {
		val result = StringBuilder()
		
		result.appendLine("modLoader=${modLoader}")
		result.appendLine("mcVersion=${mcVersion}")
		result.appendLine("loaderVersion=${loaderVersion}")
		result.appendLine("jvmVersion=${jvmVersion}")
		result.appendLine("extraArgs=[${extraArgs?.joinToString(", ") ?: ""}]")
		result.appendLine("mods=[${dependencies?.joinToString(", ") { (name, _) -> name } ?: ""}]")
		
		return result.toString()
	}
}

val smokeTestConfigs = arrayOf(
	SmokeTestConfig("fabric", "snapshot", dependencies = listOf(
		"fabric-api" to "https://github.com/FabricMC/fabric/releases/download/0.107.0%2B1.21.4/fabric-api-0.107.0+1.21.4.jar",
	)),
	SmokeTestConfig("fabric", "release", dependencies = listOf(
		"fabric-api" to "https://github.com/FabricMC/fabric/releases/download/0.107.0%2B1.21.3/fabric-api-0.107.0+1.21.3.jar",
		"modmenu" to "https://github.com/TerraformersMC/ModMenu/releases/download/v11.0.3/modmenu-11.0.3.jar",
	)),
	SmokeTestConfig("fabric", "1.21.1", dependencies = listOf(
		"fabric-api" to "https://github.com/FabricMC/fabric/releases/download/0.106.0%2B1.21.1/fabric-api-0.107.0+1.21.1.jar",
		"modmenu" to "https://github.com/TerraformersMC/ModMenu/releases/download/v11.0.3/modmenu-11.0.3.jar",
	)),
	SmokeTestConfig("fabric", "1.20.6", dependencies = listOf(
		"fabric-api" to "https://github.com/FabricMC/fabric/releases/download/0.98.0%2B1.20.6/fabric-api-0.100.8+1.20.6.jar",
		"modmenu" to "https://github.com/TerraformersMC/ModMenu/releases/download/v10.0.0/modmenu-10.0.0.jar",
	)),
	SmokeTestConfig("fabric", "1.20.1", dependencies = listOf(
		"fabric-api" to "https://github.com/FabricMC/fabric/releases/download/0.92.2%2B1.20.1/fabric-api-0.92.2+1.20.1.jar",
		"modmenu" to "https://github.com/TerraformersMC/ModMenu/releases/download/v7.2.2/modmenu-7.2.2.jar",
	)),
	SmokeTestConfig("fabric", "1.18.2", dependencies = listOf(
		"fabric-api" to "https://github.com/FabricMC/fabric/releases/download/0.77.0%2B1.18.2/fabric-api-0.77.0+1.18.2.jar",
		"modmenu" to "https://github.com/TerraformersMC/ModMenu/releases/download/v3.2.5/modmenu-3.2.5.jar",
	), extraArgs = listOf("--lwjgl=3.2.3")),
	SmokeTestConfig("fabric", "1.16.5", dependencies = listOf(
		"fabric-api" to "https://github.com/FabricMC/fabric/releases/download/0.42.0%2B1.16/fabric-api-0.42.0+1.16.jar",
		"modmenu" to "https://github.com/TerraformersMC/ModMenu/releases/download/v1.16.23/modmenu-1.16.23.jar",
	)),
	SmokeTestConfig("fabric", "1.14.4", dependencies = listOf(
		"fabric-api" to "https://github.com/FabricMC/fabric/releases/download/0.28.5%2B1.14/fabric-api-0.28.5+1.14.jar",
		"modmenu" to "https://github.com/TerraformersMC/ModMenu/releases/download/v1.7.11/modmenu-1.7.11+build.121.jar",
	)),
	SmokeTestConfig("legacyfabric", "1.12.2", dependencies = listOf(
		"legacy-fabric-api" to "https://github.com/Legacy-Fabric/fabric/releases/download/1.10.2/legacy-fabric-api-1.10.2.jar",
	)),
	SmokeTestConfig("legacyfabric", "1.8.9", dependencies = listOf(
		"legacy-fabric-api" to "https://github.com/Legacy-Fabric/fabric/releases/download/1.10.2/legacy-fabric-api-1.10.2.jar",
	)),
	SmokeTestConfig("legacyfabric", "1.7.10", dependencies = listOf(
		"legacy-fabric-api" to "https://github.com/Legacy-Fabric/fabric/releases/download/1.10.2/legacy-fabric-api-1.10.2.jar",
	)),
//	SmokeTestConfig("legacyfabric", "1.6.4", dependencies = listOf(
//		"legacy-fabric-api" to "https://github.com/Legacy-Fabric/fabric/releases/download/1.10.2/legacy-fabric-api-1.10.2.jar",
//	)),
	SmokeTestConfig("babric", "b1.7.3", jvmVersion = 17, dependencies = listOf(
		"station-api" to "https://cdn.modrinth.com/data/472oW63Q/versions/W3QVtn6S/StationAPI-2.0-alpha.2.4.jar",
	), extraArgs = listOf("--exclude-lib=asm-all")),
	SmokeTestConfig("neoforge", "release"),
	SmokeTestConfig("neoforge", "1.21.1"),
	SmokeTestConfig("neoforge", "1.20.4"),
	SmokeTestConfig("forge", "1.20.4"),
	SmokeTestConfig("forge", "1.20.1"),
	SmokeTestConfig("forge", "1.19.2"),
	SmokeTestConfig("forge", "1.18.2", extraArgs = listOf("--lwjgl=3.2.3")),
	SmokeTestConfig("forge", "1.16.5", extraArgs = listOf("--lwjgl=3.2.3")),
	SmokeTestConfig("forge", "1.14.4", dependencies = listOf(
		"mixinbootstrap" to "https://github.com/LXGaming/MixinBootstrap/releases/download/v1.1.0/_MixinBootstrap-1.1.0.jar"
	), extraArgs = listOf("--lwjgl=3.2.3")),
	SmokeTestConfig("forge", "1.12.2", dependencies = listOf(
		"mixinbooter" to "https://github.com/CleanroomMC/MixinBooter/releases/download/9.3/mixinbooter-9.3.jar"
	)),
	SmokeTestConfig("forge", "1.8.9", dependencies = listOf(
		"mixinbooter" to "https://github.com/CleanroomMC/MixinBooter/releases/download/9.3/mixinbooter-9.3.jar"
	)),
	SmokeTestConfig("forge", "1.7.10", dependencies = listOf(
		"unimixins" to "https://github.com/LegacyModdingMC/UniMixins/releases/download/0.1.19/+unimixins-all-1.7.10-0.1.19.jar"
	)),
)

@OptIn(ExperimentalPathApi::class)
val smokeTest = tasks.register("smokeTest") {
	group = "verification"
	dependsOn(tasks.checkPython, tasks.pipInstall, compressJar)
	
	val mainDir = "${project.rootDir}/.gradle/portablemc"
	
	doFirst {
		val failures = ArrayList<SmokeTestConfig>()
		smokeTestConfigs.forEach { config ->
			val name = config.hashCode().toHexString()
			val workDir = "${project.layout.buildDirectory.get()}/smoke_test/${name}"
			val modsDir = "${workDir}/mods"
			val latestLog = "${workDir}/logs/latest.log"
			
			Path(workDir).also { workPath ->
				if (!workPath.exists())
					workPath.createDirectory()
			}
			Path(modsDir).also { modsPath ->
				if (modsPath.exists())
					modsPath.deleteRecursively()
				
				modsPath.createDirectory()
			}
			Path(latestLog).also { logPath ->
				logPath.deleteIfExists()
				logPath.parent.also { logsPath ->
					if (!logsPath.exists())
						logsPath.createDirectory()
				}
			}
			
			config.dependencies?.forEach { (name, urlString) -> 
				URL(urlString).openStream().use { inputStream ->
					FileOutputStream("${modsDir}/${name}.jar").use(inputStream::transferTo)
				}
			}
			
			copy {
				from(compressJar.get().outputJar)
				into(modsDir)
			}
			
			val extraArgs = arrayListOf<String>()
			
			val jvmVersionMap = mapOf(
				17 to "java-runtime-gamma",
				21 to "java-runtime-delta",
				8 to "jre-legacy"
			)
			if (config.jvmVersion != null)
				extraArgs.add("--jvm=${mainDir}/jvm/${jvmVersionMap[config.jvmVersion]}/bin/java")
			
			if (config.extraArgs != null)
				extraArgs.addAll(config.extraArgs)
			
			exec {
				commandLine(
					"${project.rootDir}/.gradle/python/bin/portablemc",
					"--main-dir", mainDir,
					"--work-dir", workDir,
					"start", config.versionString,
					*extraArgs.toArray(),
					"--jvm-args=-DzumeGradle.auditAndExit=true",
				)
			}
			
			var passed = false
			file(latestLog).also { logFile ->
				if (logFile.exists()) {
					logFile.reader().use { reader ->
						reader.forEachLine { line ->
							if (line.endsWith("ZumeGradle audit passed"))
								passed = true
						}
					}
				}
			}
			
			if (!passed) {
				logger.error("Smoke test failed for config:\n${config}")
				failures.add(config)
			}
		}
		
		if (failures.isNotEmpty()) {
			logger.error("[{\n${failures.joinToString("}, {\n")}}]")
			error("One or more tests failed. See logs for more details.")
		}
		
		logger.info("All tests passed.")
	}
}
//endregion

afterEvaluate {
	publishing {
		repositories {
			if (!System.getenv("local_maven_url").isNullOrEmpty())
				maven(System.getenv("local_maven_url"))
		}
		
		publications {
			create<MavenPublication>("mod_id"()) {
				artifact(compressJar.get().outputJar)
				artifact(sourcesJar)
			}
		}
	}

	tasks.withType<AbstractPublishToMaven> {
		dependsOn(compressJar, sourcesJar)
	}
	
	fun getChangelog(): String {
		return file("CHANGELOG.md").readText()
	}
	
	publishMods {
		file = compressJar.get().outputJar
		additionalFiles.from(sourcesJar.get().archiveFile)
		type = releaseChannel.releaseType ?: ALPHA
		displayName = Zume.version
		version = Zume.version
		changelog = getChangelog()
		
		modLoaders.addAll("fabric", "forge", "neoforge")
		dryRun = !isRelease
		
		github {
			accessToken = providers.environmentVariable("GITHUB_TOKEN")
			repository = "Nolij/Zume"
			commitish = branchName
			tagName = releaseTagPrefix + Zume.version
		}
		
		if (dryRun.get() || releaseChannel.releaseType != null) {
			modrinth {
				accessToken = providers.environmentVariable("MODRINTH_TOKEN")
				projectId = "o6qsdrrQ"

				minecraftVersionRange {
					start = "1.14.4"
					end = "latest"

					includeSnapshots = true
				}

				minecraftVersionRange {
					start = "1.6.4"
					end = "1.12.2"

					includeSnapshots = true
				}

				minecraftVersions.add("b1.7.3")
				
				optional("embeddium")
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
					start = "1.6.4"
					end = "1.12.2"
				}
				
				minecraftVersions.add("Beta 1.7.3")
				
				val snapshots: Set<String>
				if (cfAccessToken.orNull != null) {
					val cfAPI = CurseforgeApi(cfAccessToken.get(), apiEndpoint.get())

					val mcVersions = minecraftVersions.get().map {
						"${it}-Snapshot"
					}.toHashSet()

					snapshots = 
						cfAPI.getGameVersions().map {
							it.name
						}.filter {
							it.endsWith("-Snapshot")
						}.filter { cfVersion ->
							mcVersions.contains(cfVersion)
						}.toHashSet()
				} else {
					snapshots = HashSet()
				}
				
				minecraftVersions.addAll(snapshots)

				optional("embeddium")
			}

			discord {
				webhookUrl = providers.environmentVariable("DISCORD_WEBHOOK").orElse("")

				username = "Zume Releases"

				avatarUrl = "https://github.com/Nolij/Zume/raw/master/icon_padded_large.png"

				content = changelog.map { changelog ->
					"# Zume ${Zume.version} has been released!\nChangelog: ```md\n${changelog}\n```"
				}

				setPlatforms(platforms["modrinth"], platforms["github"], platforms["curseforge"])
			}
		}
	}
	
	tasks.withType<PublishModTask> {
		dependsOn(compressJar, sourcesJar)
	}

	tasks.publishMods {
		if (!publishMods.dryRun.get() && releaseChannel.releaseType == null) {
			doLast {
				val http = HttpUtils()

				val currentTag: Tag? = releaseTags.getOrNull(0)
				val buildChangeLog =
					grgit.log {
						if (currentTag != null)
							excludes = listOf(currentTag.name)
						includes = listOf("HEAD")
					}.joinToString("\n") { commit ->
						val id = commit.abbreviatedId
						val message = commit.fullMessage.substringBefore('\n').trim()
						val author = commit.author.name
						"- [${id}] $message (${author})"
					}

				val compareStart = currentTag?.name ?: grgit.log().minBy { it.dateTime }.id
				val compareEnd = releaseTagPrefix + Zume.version
				val compareLink = "https://github.com/Nolij/Zume/compare/${compareStart}...${compareEnd}"
				
				val webhookUrl = providers.environmentVariable("DISCORD_WEBHOOK")
				val releaseChangeLog = getChangelog()
				val file = publishMods.file.asFile.get()
				
				var content = "# [Zume Test Build ${Zume.version}]" +
					"(<https://github.com/Nolij/Zume/releases/tag/${releaseTagPrefix}${Zume.version}>) has been released!\n" +
					"Changes since last build: <${compareLink}>"
				
				if (buildChangeLog.isNotBlank())
					content += " ```\n${buildChangeLog}\n```"
				content += "\nChanges since last release: ```md\n${releaseChangeLog}\n```"

				val webhook = DiscordAPI.Webhook(
					content,
					"Zume Test Builds",
					"https://github.com/Nolij/Zume/raw/master/icon_padded_large.png"
				)

				val bodyBuilder = MultipartBody.Builder()
					.setType(MultipartBody.FORM)
					.addFormDataPart("payload_json", http.json.encodeToString(webhook))
					.addFormDataPart("files[0]", file.name, file.asRequestBody("application/java-archive".toMediaTypeOrNull()))
				
				var fileIndex = 1
				for (additionalFile in publishMods.additionalFiles) {
					bodyBuilder.addFormDataPart(
						"files[${fileIndex++}]", 
						additionalFile.name, 
						additionalFile.asRequestBody(Files.probeContentType(additionalFile.toPath()).toMediaTypeOrNull())
					)
				}

				val requestBuilder = Request.Builder()
					.url(webhookUrl.get())
					.post(bodyBuilder.build())
					.header("Content-Type", "multipart/form-data")

				http.httpClient.newCall(requestBuilder.build()).execute().close()
			}
		}
	}
}