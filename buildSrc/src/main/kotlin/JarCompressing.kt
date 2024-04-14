import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.Deflater

enum class CompressionType(val id: Int?) {
	NONE(null),
	LIBDEFLATE(2),
	SEVENZIP(3),
//	ZOPFLI(4), // too slow
	;
	
	override fun toString() = name.lowercase().uppercaseFirstChar()
}

fun squishJar(jar: File, stripLVTs: Boolean, stripSourceFiles: Boolean) {
	val processClassFiles = stripLVTs || stripSourceFiles
	val contents = linkedMapOf<String, ByteArray>()
	JarFile(jar).use {
		it.entries().asIterator().forEach { entry ->
			if (!entry.isDirectory) {
				contents[entry.name] = it.getInputStream(entry).readAllBytes()
			}
		}
	}

	jar.delete()
	
	val slurper = JsonSlurper()

	JarOutputStream(jar.outputStream()).use { out ->
		out.setLevel(Deflater.BEST_COMPRESSION)
		contents.forEach { var (name, bytes) = it
			if (name.endsWith(".json") || name.endsWith(".mcmeta") || name == "mcmod.info") {
				bytes = JsonOutput.toJson(slurper.parse(bytes)).toByteArray()
			}

			if (processClassFiles && name.endsWith(".class")) {
				bytes = processClassFile(bytes, stripLVTs, stripSourceFiles)
			}

			out.putNextEntry(JarEntry(name))
			out.write(bytes)
			out.closeEntry()
		}
		out.finish()
		out.close()
	}
}

private fun processClassFile(bytes: ByteArray, stripLVTs: Boolean, stripSourceFiles: Boolean): ByteArray {
	val classNode = ClassNode()
	ClassReader(bytes).accept(classNode, 0)

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

fun deflate(zip: File, type: CompressionType) {
	if(type == CompressionType.NONE) return
	if(!advzipInstalled) {
		println("advzip is not installed, skipping re-deflation of $zip")
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