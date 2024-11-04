@file:Suppress("UnstableApiUsage")
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.nolij.zumegradle.DeflateAlgorithm
import dev.nolij.zumegradle.JsonShrinkingType
import dev.nolij.zumegradle.MixinConfigMergingTransformer
import dev.nolij.zumegradle.entryprocessing.EntryProcessors
import dev.nolij.zumegradle.task.AdvzipTask
import dev.nolij.zumegradle.task.CopyJarTask
import dev.nolij.zumegradle.task.JarEntryModificationTask
import dev.nolij.zumegradle.task.ProguardTask
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
import org.ajoberstar.grgit.Tag
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask
import xyz.wagyourtail.unimined.api.unimined
import java.nio.file.Files
import java.time.ZonedDateTime

plugins {
    id("java")
	id("maven-publish")
	id("com.github.johnrengelman.shadow")
	id("me.modmuss50.mod-publish-plugin")
	id("xyz.wagyourtail.unimined")
	id("org.ajoberstar.grgit")
}

operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

enum class ReleaseChannel(
    val suffix: String? = null,
    val releaseType: ReleaseType? = null,
    val deflation: DeflateAlgorithm = DeflateAlgorithm.INSANE,
    val json: JsonShrinkingType? = JsonShrinkingType.MINIFY,
    val proguard: Boolean = true,
	) {
	DEV_BUILD(
		suffix = "dev",
		deflation = DeflateAlgorithm.EXTRA,
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
val patch = maxPatch.let {
	if (it != null) {
		if (patchHistory.contains(it.toString())) {
			it + releaseIncrement
		} else {
			it
		}
	} else 0
}
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
		}
	} else {
		dependencies {
			implementation("dev.nolij:zson:${"zson_version"()}:downgraded-8")
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

//region compressJar
val cjTempDir = layout.buildDirectory.dir("compressJar")
val proguardJar by tasks.registering(ProguardTask::class) {
	dependsOn(tasks.shadowJar)
	inputJar = tasks.shadowJar.get().archiveFile
	destinationDirectory = cjTempDir
	run = releaseChannel.proguard
	
	config(file("proguard.pro"))
	
	mappingsFile = destinationDirectory.get().asFile
		.resolve("${archiveFile.get().asFile.nameWithoutExtension}-mappings.txt")

	jmod("java.base")
	jmod("java.desktop")

	classpath.addAll(
		uniminedImpls.flatMap { implName -> project(":$implName").unimined.minecrafts.values }.flatMap { mc ->
			val prodNamespace = mc.mcPatcher.prodNamespace

			val minecrafts = listOf(
				mc.sourceSet.compileClasspath.files,
				mc.sourceSet.runtimeClasspath.files
			)
				.flatten()
				.filter { !mc.isMinecraftJar(it.toPath()) }
				.toHashSet()

			mc.mods.getClasspathAs(prodNamespace, prodNamespace, minecrafts)
				.filter { it.extension == "jar" && !it.name.startsWith("zume") }
				.plus(mc.getMinecraft(prodNamespace, prodNamespace).toFile())
		}
	)
	
	archiveClassifier = "proguard"
}

val minifyJar by tasks.registering(JarEntryModificationTask::class) {
	dependsOn(proguardJar)
	inputJar = proguardJar.get().archiveFile
	destinationDirectory = cjTempDir

	archiveClassifier = "minified"
	json(releaseChannel.json) {
		it.endsWith(".json") || it.endsWith(".mcmeta") || it == "mcmod.info"
	}

	process(EntryProcessors.minifyClass { it.desc.startsWith("Ldev/nolij/zumegradle/proguard/") })

	if (releaseChannel.proguard) {
		process(EntryProcessors.obfuscationFixer(proguardJar.get().mappingsFile.get().asFile))
	}
}

val advzip by tasks.registering(AdvzipTask::class) {
	dependsOn(minifyJar)
	inputJar = minifyJar.get().archiveFile
	destinationDirectory = cjTempDir
	
	archiveClassifier = "advzip"
	level = releaseChannel.deflation
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

afterEvaluate {
	publishing {
		repositories {
			if (!System.getenv("local_maven_url").isNullOrEmpty())
				maven(System.getenv("local_maven_url"))
		}
		
		publications {
			create<MavenPublication>("mod_id"()) {
				artifact(compressJar.get().archiveFile)
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
		file = compressJar.get().archiveFile
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