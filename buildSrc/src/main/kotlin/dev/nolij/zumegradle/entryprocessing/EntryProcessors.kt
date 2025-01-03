package dev.nolij.zumegradle.entryprocessing

import dev.nolij.zumegradle.util.map
import dev.nolij.zumegradle.util.mappings
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import net.fabricmc.mappingio.format.MappingFormat
import org.objectweb.asm.tree.AnnotationNode
import java.io.File

import dev.nolij.zumegradle.util.toBytes
import dev.nolij.zumegradle.util.ClassNode
import org.objectweb.asm.tree.ClassNode

typealias EntryProcessor = (String, ByteArray) -> ByteArray

object EntryProcessors {
	val PASS: EntryProcessor = { _, bytes -> bytes }
	
	fun jsonMinifier(shouldRun: (String) -> Boolean = { it.endsWith(".json") }): EntryProcessor = { name, bytes ->
		if (shouldRun(name)) {
			JsonOutput.toJson(JsonSlurper().parse(bytes)).toByteArray()
		} else {
			bytes
		}
	}
	
	fun jsonPrettyPrinter(shouldRun: (String) -> Boolean = { it.endsWith(".json") }): EntryProcessor = { name, bytes ->
		if (shouldRun(name)) {
			JsonOutput.prettyPrint(String(bytes)).toByteArray()
		} else {
			bytes
		}
	}
	
	@Suppress("UNCHECKED_CAST")
	fun obfuscationFixer(mappingsFile: File, format: MappingFormat = MappingFormat.PROGUARD): EntryProcessor = { name, bytes ->
		val mappings = mappings(mappingsFile, format)
		if (name.endsWith("mixins.json")) {
			val prettyPrinted = String(bytes).contains("\n") // probably
			val json = (JsonSlurper().parse(bytes) as Map<String, Any>).toMutableMap()
			json["plugin"] = mappings.map(json["plugin"] as String)

			json["package"] = "zume.mixin" // TODO: make this configurable
			
			val result = JsonOutput.toJson(json)
			if (prettyPrinted) {
				result.toByteArray()
			} else {
				JsonOutput.prettyPrint(result).toByteArray()
			}
		} else if (name.endsWith(".class")) {
			val classNode = ClassNode(bytes)

			for (annotation in classNode.visibleAnnotations ?: emptyList()) {
				if (annotation.desc.endsWith("fml/common/Mod;")) {
					for (i in 0 until annotation.values.size step 2) {
						if (annotation.values[i] == "guiFactory") {
							val old = annotation.values[i + 1] as String
							annotation.values[i + 1] = mappings.map(old)
							println("Remapped guiFactory: $old -> ${annotation.values[i + 1]}")
						}
					}
				}
			}
				
			classNode.toBytes()
		} else {
			bytes
		}
	}
	
	fun modifyClass(modifier: (ClassNode) -> Unit): EntryProcessor = a@ { name, bytes ->
		if (!name.endsWith(".class")) {
			return@a bytes
		}

		ClassNode(bytes).apply(modifier).toBytes()
	}
	
	fun removeAnnotations(extraAnnotationsToStrip: (AnnotationNode) -> Boolean): EntryProcessor = modifyClass { classNode ->
		val shouldStripAnnotation: (AnnotationNode) -> Boolean = {
			setOf(
				"Lorg/spongepowered/asm/mixin/Dynamic;",
				"Lorg/spongepowered/asm/mixin/Final;",
				"Ljava/lang/SafeVarargs;",
			).contains(it.desc)
				|| it.desc.startsWith("Lorg/jetbrains/annotations/")
				|| extraAnnotationsToStrip(it)
		}
		
		classNode.invisibleAnnotations?.removeIf(shouldStripAnnotation)
		classNode.visibleAnnotations?.removeIf(shouldStripAnnotation)
		classNode.invisibleTypeAnnotations?.removeIf(shouldStripAnnotation)
		classNode.visibleTypeAnnotations?.removeIf(shouldStripAnnotation)
		classNode.fields.forEach {
			it.invisibleAnnotations?.removeIf(shouldStripAnnotation)
			it.visibleAnnotations?.removeIf(shouldStripAnnotation)
			it.invisibleTypeAnnotations?.removeIf(shouldStripAnnotation)
			it.visibleTypeAnnotations?.removeIf(shouldStripAnnotation)
		}
		classNode.methods.forEach {
			it.invisibleAnnotations?.removeIf(shouldStripAnnotation)
			it.visibleAnnotations?.removeIf(shouldStripAnnotation)
			it.invisibleTypeAnnotations?.removeIf(shouldStripAnnotation)
			it.visibleTypeAnnotations?.removeIf(shouldStripAnnotation)
			it.invisibleLocalVariableAnnotations?.removeIf(shouldStripAnnotation)
			it.visibleLocalVariableAnnotations?.removeIf(shouldStripAnnotation)
			it.invisibleParameterAnnotations?.forEach { parameterAnnotations ->
				parameterAnnotations?.removeIf(shouldStripAnnotation)
			}
			it.visibleParameterAnnotations?.forEach { parameterAnnotations ->
				parameterAnnotations?.removeIf(shouldStripAnnotation)
			}
		}

		if (classNode.invisibleAnnotations?.any { it.desc == "Lorg/spongepowered/asm/mixin/Mixin;" } == true) {
			// remove any empty constructors that just call super()
			// since these classes are never loaded, they are not needed
			// 3 instructions are ALOAD + call to super() + RETURN
			classNode.methods.removeAll { it.name == "<init>" && it.instructions.size() <= 3 }
		}
	}
}