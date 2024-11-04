package dev.nolij.zumegradle.task

import dev.nolij.zumegradle.DeflateAlgorithm
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

@Suppress("LeakingThis")
abstract class AdvzipTask : ProcessJarTask() {
	@get:Input
	abstract val level: Property<DeflateAlgorithm>
	
	@get:Input
	abstract val throwIfNotInstalled: Property<Boolean>
	
	init {
		throwIfNotInstalled.convention(false)
	}
	
	override fun process() {
		if(try {
			ProcessBuilder("advzip", "-V").start().waitFor() != 0
		} catch (e: Exception) { true }) {
			if(throwIfNotInstalled.get()) {
				throw IllegalStateException("advzip is not installed")
			}
			
			println("advzip is not installed, skipping ${this.name}")
			return
		}
		
		val jar = inputJar.get().asFile.copyTo(archiveFile.get().asFile, true)
		val type = level.get()

		try {
			val process = ProcessBuilder("advzip", "-z", "-${type.id}", jar.absolutePath).start()
			val exitCode = process.waitFor()
			if (exitCode != 0) {
				error("Failed to compress $jar with $type")
			}
		} catch (e: Exception) {
			error("Failed to compress $jar with $type: ${e.message}")
		}
	}
}