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
				bytes = when(jsonProcessing) {
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
	if(type == JarShrinkingType.NONE) return
	if(!advzipInstalled) {
		println("advzip is not installed, skipping re-deflation of $zip")
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

open class CompressJarTask : DefaultTask() {
	@InputFile
	lateinit var inputJar: File

	@Input
	var classShrinkingType = ClassShrinkingType.STRIP_ALL

	@Input
	var jarShrinkingType = JarShrinkingType.LIBDEFLATE
	
	@Input
	var jsonShrinkingType = JsonShrinkingType.NONE

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

	@TaskAction
	fun compressJar() {
		squishJar(inputJar, classShrinkingType, jsonShrinkingType)
		deflate(outputJar, jarShrinkingType)
	}
}