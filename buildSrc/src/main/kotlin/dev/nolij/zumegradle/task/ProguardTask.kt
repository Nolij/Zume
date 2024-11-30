package dev.nolij.zumegradle.task

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.Optional
import proguard.Configuration
import proguard.ConfigurationParser
import proguard.ProGuard
import java.io.File
import java.util.*

private val javaHome = System.getProperty("java.home")!!

abstract class ProguardTask : ProcessJarTask() {
	
	@get:InputFiles
	@get:Classpath
	abstract val classpath: ListProperty<File>
	
	@get:Input
	abstract val options: ListProperty<String>
	
	@get:Input
	abstract val run: Property<Boolean>
	
	@get:OutputFile
	@get:Optional
	abstract val mappingsFile: RegularFileProperty
	
	fun config(config: File) {
		options.add("@${config.relativeTo(project.rootDir)}")
	}
	
	fun jmod(jmod: String) {
		classpath.add(File("$javaHome/jmods/$jmod.jmod"))
	}
	
	override fun process() {
		if(!run.get()) {
			inputJar.get().asFile.copyTo(archiveFile.get().asFile, true)
			return
		}
		
		val cmd = this.options.get().toMutableSet()
		
		cmd.addAll(arrayOf(
			"-injars", inputJar.get().asFile.absolutePath,
			"-outjars", archiveFile.get().asFile.absolutePath
		))
		
		if (mappingsFile.isPresent) {
			cmd.add("-printmapping")
			cmd.add(mappingsFile.get().asFile.absolutePath)
		}
		
		cmd.addAll(arrayOf(
			"-libraryjars", classpath.get()
				.toSet()
				.sortedBy { it.name }
				.joinToString(File.pathSeparator) { "\"${it.absolutePath}\"" }
		))

		val debug = Properties().apply {
			val gradleproperties = project.rootDir.resolve("gradle.properties")
			if (gradleproperties.exists()) {
				load(gradleproperties.inputStream())
			}
		}.getProperty("zumegradle.proguard.keepAttrs")?.toBoolean() ?: false

		if (debug) {
			cmd.add("-keepattributes")
			cmd.add("*Annotation*,SourceFile,MethodParameters,L*Table")
			cmd.add("-dontobfuscate")
		}

		project.logger.debug("Proguard command: {}", cmd)

		val configuration = Configuration()
		ConfigurationParser(cmd.toTypedArray(), System.getProperties())
			.parse(configuration)

		try {
			ProGuard(configuration).execute()
		} catch (ex: Exception) {
			throw IllegalStateException("ProGuard failed for task ${this.name}", ex)
		}
	}
}