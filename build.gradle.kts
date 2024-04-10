import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
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
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.Deflater

plugins {
    id("java")
	id("maven-publish")
	id("com.github.johnrengelman.shadow")
	id("me.modmuss50.mod-publish-plugin")
	id("xyz.wagyourtail.unimined")
	id("org.ajoberstar.grgit")
}

operator fun String.invoke(): String {
	return (rootProject.properties[this] as String?)!!
}

enum class ReleaseChannel(
	val suffix: String? = null,
	val releaseType: ReleaseType? = null,
	val compress: Boolean,
	) {
	DEV_BUILD(suffix = "dev", compress = false),
	PRE_RELEASE(suffix = "pre", compress = false),
	RELEASE_CANDIDATE(suffix = "rc", compress = true),
	RELEASE(releaseType = ReleaseType.STABLE, compress = true),
}

val isRelease = rootProject.hasProperty("release_channel")
val releaseChannel = if (isRelease) ReleaseChannel.valueOf("release_channel"()) else ReleaseChannel.DEV_BUILD

project.exec {
	commandLine("git", "fetch", "--all", "--tags")
}

val headDateTime = grgit.head().dateTime

val branchName = grgit.branch.current().name!!
val releaseTagPrefix = "release/"

val releaseTags = grgit.tag.list()
	.filter { tag -> tag.name.startsWith(releaseTagPrefix) }
	.sortedByDescending { tag -> tag.commit.dateTime }
	.dropWhile { tag -> tag.commit.dateTime > headDateTime }

val currentTag = releaseTags.getOrNull(0)

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
			1
		else
			0
	) ?: 0
var patchAndSuffix = patch.toString()

if (releaseChannel.suffix != null) {
	patchAndSuffix += "-${releaseChannel.suffix}"
	
	if (isRelease) {
		patchAndSuffix += "."
		
		val maxBuild = patchHistory
			.mapNotNull { it.removePrefix(patchAndSuffix).toIntOrNull() }
			.maxOrNull()
		
		val build = (maxBuild?.plus(1)) ?: 1
		patchAndSuffix += build.toString()
	}
}

Zume.version = "${minorVersion}.${patchAndSuffix}"
println("Zume Version: ${Zume.version}")

rootProject.group = "maven_group"()
rootProject.version = Zume.version

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
		mavenCentral {
			content {
				excludeGroup("ca.weblite")
			}
		}
		maven("https://repo.spongepowered.org/maven")
		maven("https://jitpack.io/")
		maven("https://api.modrinth.com/maven") {
			content {
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

	tasks.processResources {
		inputs.file(rootDir.resolve("gradle.properties"))
		inputs.property("version", Zume.version)

		filteringCharset = "UTF-8"

		val props = mutableMapOf<String, String>()
		props.putAll(rootProject.properties
			.filterValues { value -> value is String }
			.mapValues { entry -> entry.value as String })
		props["mod_version"] = Zume.version

		filesMatching(immutableListOf("fabric.mod.json", "mcmod.info", "META-INF/mods.toml")) {
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
		archivesName = "${"archives_base_name"()}-${subProject.name}"
	}
	
	tasks.withType<GenerateModuleMetadata> {
		enabled = false
	}

	if (implName in uniminedImpls) {
		apply(plugin = "xyz.wagyourtail.unimined")
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
		
		tasks.processResources {
			from("common/src/main/resources")
		}
		
		afterEvaluate {
			val platformJar = tasks.create<ShadowJar>("platformJar") {
				group = "build"
				
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
					
						if (implName in legacyForgeImpls) {
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

	compileOnly("org.apache.logging.log4j:log4j-core:${"log4j_version"()}")
	
	compileOnly(project(":stubs"))
	
	"shade"(project(":common")) { isTransitive = false }
	
	uniminedImpls.forEach { 
		implementation(project(":${it}")) { isTransitive = false }
	}
}

tasks.processResources {
	from("common/src/main/resources")
}

tasks.jar {
	enabled = false
}

tasks.shadowJar {
	val shadowJar = this
	from("LICENSE") {
		rename { "${it}_${"archives_base_name"()}" }
	}
	
	exclude("*.xcf")
	
	configurations = immutableListOf(project.configurations["shade"])
	archiveClassifier = null
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
	
	uniminedImpls.forEach { impl ->
		val remapJar = project(":${impl}").tasks.withType<RemapJarTask>()["remapJar"]
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

tasks.assemble {
	dependsOn(tasks.shadowJar)
}

abstract class ProcessJarTask : DefaultTask() {
	@get:InputFile
	abstract val inputJar: RegularFileProperty
	
	@OutputFile
	fun getOutputJar(): RegularFile {
		return inputJar.get()
	}
}

val compressJar = tasks.register<ProcessJarTask>("compressJar") {
	dependsOn(tasks.shadowJar)
	group = "build"
	
	val stripLVTs = "strip_lvts"().toBoolean()
	val stripSourceFiles = "strip_source_files"().toBoolean()
	
	inputs.property("strip_lvts", stripLVTs)
	inputs.property("strip_source_files", stripSourceFiles)
	
	val processClassFiles = stripLVTs || stripSourceFiles
	
	val shadowJar = tasks.shadowJar.get()
	inputJar.set(shadowJar.archiveFile)
	
	doLast {
		val jar = inputJar.get().asFile
		val contents = linkedMapOf<String, ByteArray>()
		JarFile(jar).use {
			it.entries().asIterator().forEach { entry ->
				if (!entry.isDirectory) {
					contents[entry.name] = it.getInputStream(entry).readAllBytes()
				}
			}
		}

		jar.delete()

		JarOutputStream(jar.outputStream()).use { out ->
			out.setLevel(Deflater.BEST_COMPRESSION)
			contents.forEach { var (name, bytes) = it
				if (name.endsWith(".json") || name.endsWith(".mcmeta") || name == "mcmod.info") {
					bytes = JsonOutput.toJson(JsonSlurper().parse(bytes)).toByteArray()
				}

				if (processClassFiles && name.endsWith(".class")) {
					val reader = ClassReader(bytes)
					val classNode = ClassNode()
					reader.accept(classNode, 0)

					if (stripLVTs) {
						classNode.methods.forEach { methodNode ->
							methodNode.localVariables?.clear()
							methodNode.parameters?.clear()
						}
					}
					if (stripSourceFiles) {
						classNode.sourceFile = null
					}

					val writer = ClassWriter(0)
					classNode.accept(writer)
					bytes = writer.toByteArray()
				}

				out.putNextEntry(JarEntry(name))
				out.write(bytes)
				out.closeEntry()
			}
			out.finish()
			out.close()
		}
	}
}

afterEvaluate {
	publishing {
		publications {
			create<MavenPublication>("archives_base_name"()) {
				artifact(tasks.shadowJar)
				uniminedImpls.forEach { implName ->
					artifact(project(":${implName}").tasks.named("platformJar"))
				}
			}
		}
	}

	tasks.withType<AbstractPublishToMaven> {
		dependsOn(tasks.shadowJar)
		uniminedImpls.forEach { implName ->
			dependsOn(project(":${implName}").tasks.named("platformJar"))
		}
	}
	
	fun getChangelog(): String {
		return file("CHANGELOG.md").readText()
	}

	fun getTaskForPublish(): TaskProvider<out DefaultTask> {
		return if (releaseChannel.compress)
			compressJar
		else
			tasks.shadowJar
	}
	
	fun getFileForPublish(): RegularFile {
		return if (releaseChannel.compress)
			compressJar.get().getOutputJar()
		else
			tasks.shadowJar.get().archiveFile.get()
	}
	
	publishMods {
		file = getFileForPublish()
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
				
				snapshots.add("1.20.5-Snapshot")
				
				minecraftVersions.addAll(snapshots)
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
		dependsOn(getTaskForPublish())
	}

	tasks.publishMods {
		if (releaseChannel.releaseType == null) {
			doLast {
				val http = HttpUtils()
				
				val buildChangeLog =
					grgit.log {
						range(currentTag!!.commit.id, "HEAD")
					}.joinToString("\n") { commit ->
						"- [${commit.abbreviatedId}] ${commit.fullMessage.trim()} (${commit.author.name})"
					}
				
				val webhookUrl = providers.environmentVariable("DISCORD_WEBHOOK")
				val releaseChangeLog = getChangelog()
				val file = getFileForPublish().asFile
				
				var content = "# [Zume Test Build ${Zume.version}](<https://github.com/Nolij/Zume/releases/tag/release/${Zume.version}>) has been released!\n"
				
				content += 
					if (buildChangeLog.trim().isEmpty())
						"Changes since last build: None"
					else
						"Changes since last build: ```md\n${buildChangeLog}\n```\n"
				content += "Changes since last release: ```md\n${releaseChangeLog}\n```"

				val webhook = DiscordAPI.Webhook(
					content,
					"Zume Test Builds",
					"https://github.com/Nolij/Zume/raw/master/icon_padded_large.png"
				)

				val bodyBuilder = MultipartBody.Builder()
					.setType(MultipartBody.FORM)
					.addFormDataPart("payload_json", http.json.encodeToString(webhook))
					.addFormDataPart("files[0]", file.name, file.asRequestBody("application/java-archive".toMediaTypeOrNull()))

				val requestBuilder = Request.Builder()
					.url(webhookUrl.get())
					.post(bodyBuilder.build())
					.header("Content-Type", "multipart/form-data")

				http.httpClient.newCall(requestBuilder.build()).execute().close()
			}
		}
	}
}