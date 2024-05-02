package dev.nolij.zumegradle

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
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
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.Deflater

enum class JarShrinkingType(val id: Int?) {
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

fun squishJar(jar: File, classProcessing: ClassShrinkingType, jsonProcessing: JsonShrinkingType) {
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

	JarOutputStream(jar.outputStream()).use { out ->
		out.setLevel(Deflater.BEST_COMPRESSION)
		contents.forEach { var (name, bytes) = it
			if (jsonProcessing != JsonShrinkingType.NONE && 
				name.endsWith(".json") || name.endsWith(".mcmeta") || name == "mcmod.info") {
				bytes = when (jsonProcessing) {
					JsonShrinkingType.MINIFY -> JsonOutput.toJson(json.parse(bytes)).toByteArray()
					JsonShrinkingType.PRETTY_PRINT -> JsonOutput.prettyPrint(JsonOutput.toJson(json.parse(bytes))).toByteArray()
					else -> bytes
				}
			}

			if (name.endsWith(".class")) {
				bytes = processClassFile(bytes, classProcessing)
			}

			out.putNextEntry(JarEntry(name))
			out.write(bytes)
			out.closeEntry()
		}
		out.finish()
		out.close()
	}
}

private fun processClassFile(bytes: ByteArray, classFileSettings: ClassShrinkingType): ByteArray {
	if(!classFileSettings.shouldRun()) return bytes
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

	val writer = ClassWriter(0)
	classNode.accept(writer)
	return writer.toByteArray()
}

val advzipInstalled = isAdvzipInstalled()

private fun isAdvzipInstalled(): Boolean {
	return try {
		ProcessBuilder("advzip", "-V").start().waitFor() == 0
	} catch (e: Exception) {
		false
	}
}

fun deflate(zip: File, type: JarShrinkingType) {
	if (type == JarShrinkingType.NONE) return
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
fun applyProguard(outputJar: File, minecraftConfigs: List<MinecraftConfig>) {
	val inputJar = outputJar.copyTo(
		outputJar.parentFile.resolve("${outputJar.nameWithoutExtension}_.jar"), true)
	inputJar.deleteOnExit()
	
	val proguardCommand = ArrayList<String>()
	proguardCommand.addAll(arrayOf(
		"-ignorewarnings", "-dontnote",
		"-optimizationpasses", "10",
		"-optimizations", "!class/merging/*,!method/marking/private,!*/specialization/*",
		"-allowaccessmodification",
		"-optimizeaggressively",
		"-overloadaggressively",
		"-repackageclasses", "dev.nolij.zume",
		"-printmapping", outputJar.parentFile.resolve("${outputJar.nameWithoutExtension}-mappings.txt").absolutePath,
		"-injars", inputJar.absolutePath,
		"-outjars", outputJar.absolutePath,
		"-keepattributes", "Runtime*Annotations", // keep annotations
		"-keep,allowoptimization", "public class dev.nolij.zume.api.** { public *; }", // public APIs
		"-keepclassmembers", "class dev.nolij.zume.impl.config.ZumeConfigImpl { public <fields>; }", // dont rename config fields
		"-keep,allowoptimization", "class dev.nolij.zume.ZumeMixinPlugin", // dont rename mixin plugin
		"-keep", "class dev.nolij.zume.mixin.** { *; }", // dont touch mixins
		"-keep,allowobfuscation", "@*.*.fml.common.Mod class dev.nolij.zume.** { " +
			"public <init>(...); }",
		"-keep,allowobfuscation", "class dev.nolij.zume.** implements dev.nolij.zume.api.platform.v0.IZumeImplementation { " + // entrypoints
			"@*.*.fml.common.Mod\$EventHandler <methods>; " +
			"@*.*.fml.common.eventhandler.SubscribeEvent <methods>; }",
		"-keepclassmembers", "class dev.nolij.zume.** extends net.minecraft.** { " + // screens
			"void render(int,int,float); " +
			"void tick(); " +
			"void init(); }",
		"-keepclassmembers,allowoptimization", "class dev.nolij.zume.** extends net.minecraft.client.gui.screens.Screen { public *; }",
		"-keep,allowoptimization", "class dev.nolij.zume.** implements *.*.fml.client.IModGuiFactory", // Legacy Forge config providers
		"-keep,allowoptimization", "class dev.nolij.zume.** extends *.*.fml.client.config.GuiConfig { *; }", // Legacy Forge config providers
		"-keep,allowoptimization", "class dev.nolij.zume.FabricZumeBootstrapper", // referenced in FMJ
		"-keep,allowoptimization", "class dev.nolij.zume.modern.integration.ZumeModMenuIntegration", // referenced in FMJ
		"-keep,allowoptimization", "class dev.nolij.zume.primitive.event.KeyBindingRegistrar { public *; }", // referenced in FMJ
		"-keep,allowoptimization", "class io.github.prospector.modmenu.** { *; }", // ugly classloader hack
	))
	
	val libraries = HashSet<String>()
	libraries.add("${JAVA_HOME}/jmods/java.base.jmod")
	libraries.add("${JAVA_HOME}/jmods/java.desktop.jmod")

	for (minecraftConfig in minecraftConfigs) {
		val prodNamespace = minecraftConfig.mcPatcher.prodNamespace
		
		libraries.add(minecraftConfig.getMinecraft(prodNamespace, prodNamespace).toFile().absolutePath)
		
		libraries.addAll(minecraftConfig.mods.getClasspathAs(prodNamespace, prodNamespace,
				minecraftConfig.sourceSet.compileClasspath.files
					.filter { !minecraftConfig.isMinecraftJar(it.toPath()) }
					.toSet())
			.filter { it.extension == "jar" }
			.filter { !it.name.startsWith("zume") }
			.map { it.absolutePath })
	}
	
	proguardCommand.add("-libraryjars")
	proguardCommand.add(libraries.joinToString(":") { "\"$it\"" })

	val configuration = Configuration()
	ConfigurationParser(proguardCommand.toTypedArray(), System.getProperties()).use { parser ->
		parser.parse(configuration)
	}
	
	try {
		ProGuard(configuration).execute()
	} catch(ex: Exception) {
		throw IllegalStateException("ProGuard failed for $outputJar", ex)
	} finally {
		inputJar.delete()
	}
}

open class CompressJarTask : DefaultTask() {
	@InputFile
	lateinit var inputJar: File

	@Input
	var classShrinkingType = ClassShrinkingType.STRIP_ALL

	@Input
	var jarShrinkingType = JarShrinkingType.LIBDEFLATE
	
	@Input
	var jsonShrinkingType = JsonShrinkingType.NONE
	
	@get:Input
	val useProguard get() = this.minecraftConfigs != null
	
	private var minecraftConfigs: List<MinecraftConfig>? = null

	@get:OutputFile
	val outputJar: File
		get() = inputJar // compressed jar will replace the input jar
	
	@Option(option = "class-file-compression", description = "How to process class files")
	fun setClassShrinkingType(value: String) {
		classShrinkingType = ClassShrinkingType.valueOf(value.uppercase())
	}
	
	@Option(option = "compression-type", description = "How to compress the jar")
	fun setJarShrinkingType(value: String) {
		jarShrinkingType = value.uppercase().let {
			if(it.matches(Regex("7Z(?:IP)?"))) JarShrinkingType.SEVENZIP
			else JarShrinkingType.valueOf(it.uppercase())
		}
	}
	
	@Option(option = "json-processing", description = "How to process json files")
	fun setJsonShrinkingType(value: String) {
		jsonShrinkingType = JsonShrinkingType.valueOf(value.uppercase())
	}
	
	fun useProguard(minecraftConfigs: List<MinecraftConfig>?) {
		this.minecraftConfigs = minecraftConfigs
	}

	@TaskAction
	fun compressJar() {
		squishJar(inputJar, classShrinkingType, jsonShrinkingType)
		deflate(outputJar, jarShrinkingType)
		if (useProguard)
			applyProguard(outputJar, minecraftConfigs!!)
	}
}