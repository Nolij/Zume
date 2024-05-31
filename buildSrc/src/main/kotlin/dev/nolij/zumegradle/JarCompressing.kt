package dev.nolij.zumegradle

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import net.fabricmc.mappingio.MappingReader
import net.fabricmc.mappingio.format.MappingFormat
import net.fabricmc.mappingio.tree.MappingTree.ClassMapping
import net.fabricmc.mappingio.tree.MemoryMappingTree
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import proguard.Configuration
import proguard.ConfigurationParser
import proguard.ProGuard
import xyz.wagyourtail.unimined.api.minecraft.MinecraftConfig
import java.io.File
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.Deflater

enum class DeflateAlgorithm(val id: Int?) {
	NONE(null),
	LIBDEFLATE(2),
	SEVENZIP(3),
//	ZOPFLI(4), // too slow
	;

	override fun toString() = name.lowercase().uppercaseFirstChar()
}

enum class ClassShrinkingType {
	STRIP_NONE,
	STRIP_LVTS,
	STRIP_SOURCE_FILES,
	STRIP_ALL,
	;

	fun shouldStripLVTs() = this == STRIP_LVTS || this == STRIP_ALL
	fun shouldStripSourceFiles() = this == STRIP_SOURCE_FILES || this == STRIP_ALL
	fun shouldRun() = this != STRIP_NONE
}

enum class JsonShrinkingType {
	NONE, MINIFY, PRETTY_PRINT
}

fun squishJar(jar: File, classProcessing: ClassShrinkingType, jsonProcessing: JsonShrinkingType, mappingsFile: File?) {
	val contents = linkedMapOf<String, ByteArray>()
	JarFile(jar).use {
		it.entries().asIterator().forEach { entry ->
			if (!entry.isDirectory) {
				contents[entry.name] = it.getInputStream(entry).readAllBytes()
			}
		}
	}

	jar.delete()

	val json = JsonSlurper()

	val isObfuscating = mappingsFile?.exists() == true
	val mappings = if (isObfuscating) mappings(mappingsFile!!) else null

	JarOutputStream(jar.outputStream()).use { out ->
		out.setLevel(Deflater.BEST_COMPRESSION)
		contents.forEach {
			var (name, bytes) = it
			if (name == "fabric.mod.json" && isObfuscating) {
				bytes = remapFMJ(bytes, mappings!!)
			}

			if (name.endsWith("mixins.json") && isObfuscating) {
				bytes = remapMixinConfig(bytes, mappings!!)
			}

			if (jsonProcessing != JsonShrinkingType.NONE &&
				name.endsWith(".json") || name.endsWith(".mcmeta") || name == "mcmod.info"
			) {
				bytes = when (jsonProcessing) {
					JsonShrinkingType.MINIFY -> JsonOutput.toJson(json.parse(bytes)).toByteArray()
					JsonShrinkingType.PRETTY_PRINT -> JsonOutput.prettyPrint(JsonOutput.toJson(json.parse(bytes)))
						.toByteArray()

					else -> throw AssertionError()
				}
			}

			if (name.endsWith(".class")) {
				bytes = processClassFile(bytes, classProcessing, mappings!!)
			}

			out.putNextEntry(JarEntry(name))
			out.write(bytes)
			out.closeEntry()
		}
		out.finish()
	}
}

@Suppress("UNCHECKED_CAST")
private fun remapFMJ(bytes: ByteArray, mappings: MemoryMappingTree): ByteArray {
	val json = (JsonSlurper().parse(bytes) as Map<String, Any>).toMutableMap()
	var entrypoints = (json["entrypoints"] as Map<String, List<String>>?)?.toMutableMap()
	if (entrypoints == null) {
		throw IllegalStateException("fabric.mod.json does not contain entrypoints")
	}

	val newEntrypoints = mutableMapOf<String, MutableList<String>>()
	for ((type, classes) in entrypoints) {
		for (old in classes) {
			val obf = mappings.obfuscate(old)
			newEntrypoints.computeIfAbsent(type) { mutableListOf() }.add(obf)
		}
	}

	json["entrypoints"] = newEntrypoints

	return JsonOutput.toJson(json).toByteArray()
}

@Suppress("UNCHECKED_CAST")
private fun remapMixinConfig(bytes: ByteArray, mappings: MemoryMappingTree): ByteArray {
	val json = (JsonSlurper().parse(bytes) as Map<String, Any>).toMutableMap()
	val old = json["plugin"] as String
	val obf = mappings.obfuscate(old)
	json["plugin"] = obf

	json["package"] = "zume.mixin"

	return JsonOutput.toJson(json).toByteArray()
}

private fun processClassFile(bytes: ByteArray, classFileSettings: ClassShrinkingType, mappings: MemoryMappingTree): ByteArray {
	val classNode = ClassNode()
	ClassReader(bytes).accept(classNode, 0)

	if (classFileSettings.shouldStripLVTs()) {
		classNode.methods.forEach { methodNode ->
			methodNode.localVariables?.clear()
			methodNode.parameters?.clear()
		}
	}
	if (classFileSettings.shouldStripSourceFiles()) {
		classNode.sourceFile = null
	}
	
	for(annotation in classNode.visibleAnnotations ?: emptyList()) {
		if(annotation.desc.endsWith("fml/common/Mod;")) {
			for (i in 0 until annotation.values.size step 2) {
				if (annotation.values[i] == "guiFactory") {
					var old = annotation.values[i + 1] as String
					annotation.values[i + 1] = mappings.obfuscate(old)
					println("Remapped guiFactory: $old -> ${annotation.values[i + 1]}")
				}
			}
		}
	}

	if (classNode.invisibleAnnotations?.map { it.desc }?.contains("Lorg/spongepowered/asm/mixin/Mixin;") == true) {
		classNode.methods.removeAll { it.name == "<init>" && it.instructions.size() <= 3 } // ALOAD, super(), RETURN
	}

	val writer = ClassWriter(0)
	classNode.accept(writer)
	return writer.toByteArray()
}

val advzipInstalled = try {
	ProcessBuilder("advzip", "-V").start().waitFor() == 0
} catch (e: Exception) {
	false
}

fun deflate(zip: File, type: DeflateAlgorithm) {
	if (type == DeflateAlgorithm.NONE) return
	if (!advzipInstalled) {
		println("advzip is not installed; skipping re-deflation of $zip")
		return
	}

	try {
		val process = ProcessBuilder("advzip", "-z", "-${type.id}", zip.absolutePath).start()
		val exitCode = process.waitFor()
		if (exitCode != 0) {
			error("Failed to compress $zip with $type")
		}
	} catch (e: Exception) {
		error("Failed to compress $zip with $type: ${e.message}")
	}
}

val JAVA_HOME = System.getProperty("java.home")

@Suppress("UnstableApiUsage")
fun applyProguard(jar: File, minecraftConfigs: List<MinecraftConfig>, configDir: File) {
	val inputJar = jar.copyTo(
		jar.parentFile.resolve(".${jar.nameWithoutExtension}_proguardRunning.jar"), true
	).also {
		it.deleteOnExit()
	}

	val config = configDir.resolve("proguard.pro")
	if (!config.exists()) {
		error("proguard.pro not found")
	}
	val proguardCommand = mutableListOf(
		"@${config.absolutePath}",
		"-printmapping", jar.parentFile.resolve("${jar.nameWithoutExtension}-mappings.txt").absolutePath,
		"-injars", inputJar.absolutePath,
		"-outjars", jar.absolutePath,
	)

	val libraries = HashSet<String>()
	libraries.add("${JAVA_HOME}/jmods/java.base.jmod")
	libraries.add("${JAVA_HOME}/jmods/java.desktop.jmod")

	for (minecraftConfig in minecraftConfigs) {
		val prodNamespace = minecraftConfig.mcPatcher.prodNamespace

		libraries.add(minecraftConfig.getMinecraft(prodNamespace, prodNamespace).toFile().absolutePath)

		val minecrafts = listOf(
			minecraftConfig.sourceSet.compileClasspath.files,
			minecraftConfig.sourceSet.runtimeClasspath.files
		)
			.flatten()
			.filter { it: File -> !minecraftConfig.isMinecraftJar(it.toPath()) }
			.toHashSet()

		libraries += minecraftConfig.mods.getClasspathAs(prodNamespace, prodNamespace, minecrafts)
			.filter { it.extension == "jar" && !it.name.startsWith("zume") }
			.map { it.absolutePath }
	}

	val debug = Properties().apply {
		val gradleproperties = configDir.resolve("gradle.properties")
		if (gradleproperties.exists()) {
			load(gradleproperties.inputStream())
		}
	}.getProperty("zumegradle.proguard.keepAttrs").toBoolean()

	if (debug) {
		proguardCommand.add("-keepattributes")
		proguardCommand.add("*Annotation*,SourceFile,MethodParameters,L*Table")
		proguardCommand.add("-dontobfuscate")
	}

	proguardCommand.add("-libraryjars")
	proguardCommand.add(libraries.joinToString(File.pathSeparator) { "\"$it\"" })

	val configuration = Configuration()
	ConfigurationParser(proguardCommand.toTypedArray(), System.getProperties())
		.parse(configuration)

	try {
		ProGuard(configuration).execute()
	} catch (ex: Exception) {
		throw IllegalStateException("ProGuard failed for $jar", ex)
	} finally {
		inputJar.delete()
	}
}

open class CompressJarTask : DefaultTask() {
	@InputFile
	lateinit var inputJar: File

	@Input
	var classShrinkingType = ClassShrinkingType.STRIP_ALL
		get() = if (useProguard) ClassShrinkingType.STRIP_NONE else field

	@Input
	var deflateAlgorithm = DeflateAlgorithm.LIBDEFLATE

	@Input
	var jsonShrinkingType = JsonShrinkingType.NONE

	@get:Input
	val useProguard get() = !this.minecraftConfigs.isEmpty()

	private var minecraftConfigs: List<MinecraftConfig> = emptyList()

	@get:OutputFile
	val outputJar get() = inputJar // compressed jar will replace the input jar

	@get:OutputFile
	@get:Optional
	val mappingsFile
		get() = if (useProguard)
			inputJar.parentFile.resolve("${inputJar.nameWithoutExtension}-mappings.txt")
		else null

	@Option(option = "class-file-compression", description = "How to process class files")
	fun setClassShrinkingType(value: String) {
		classShrinkingType = ClassShrinkingType.valueOf(value.uppercase())
	}

	@Option(option = "compression-type", description = "How to recompress the jar")
	fun setDeflateAlgorithm(value: String) {
		deflateAlgorithm = value.uppercase().let {
			if (it.matches(Regex("7Z(?:IP)?"))) DeflateAlgorithm.SEVENZIP
			else DeflateAlgorithm.valueOf(it)
		}
	}

	@Option(option = "json-processing", description = "How to process json files")
	fun setJsonShrinkingType(value: String) {
		jsonShrinkingType = JsonShrinkingType.valueOf(value.uppercase())
	}

	fun useProguard(minecraftConfigs: List<MinecraftConfig>) {
		this.minecraftConfigs = minecraftConfigs
	}

	@TaskAction
	fun compressJar() {
		if (useProguard)
			applyProguard(inputJar, minecraftConfigs, project.rootDir)
		squishJar(inputJar, classShrinkingType, jsonShrinkingType, mappingsFile)
		deflate(outputJar, deflateAlgorithm)
	}
}

fun mappings(file: File, format: MappingFormat = MappingFormat.PROGUARD): MemoryMappingTree {
	if (!file.exists()) {
		error("Mappings file $file does not exist")
	}

	val mappingTree = MemoryMappingTree()
	MappingReader.read(file.toPath(), format, mappingTree)
	return mappingTree
}

@Suppress("INACCESSIBLE_TYPE", "NAME_SHADOWING")
fun MemoryMappingTree.obfuscate(src: String): String {
	val src = src.replace('.', '/')
	val dstNamespaceIndex = getNamespaceId(dstNamespaces[0])
	val classMapping: ClassMapping? = getClass(src)
	if (classMapping == null) {
		println("Class $src not found in mappings")
		return src
	}
	return classMapping.getDstName(dstNamespaceIndex).replace('/', '.')
}