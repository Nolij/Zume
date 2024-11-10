package dev.nolij.zumegradle.task

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.jvm.tasks.Jar

@Suppress("LeakingThis")
abstract class ProcessJarTask : Jar() {
	@get:InputFile
	@get:PathSensitive(PathSensitivity.RELATIVE)
	abstract val inputJar: RegularFileProperty
	
	@get:Input
	abstract val ignoreSameInputOutput: Property<Boolean>
	
	init { 
		ignoreSameInputOutput.convention(false)
		
		group = "processing"
	}
	
	override fun copy() {
		if(!ignoreSameInputOutput.get() && inputJar.get().asFile.equals(archiveFile.get().asFile)) {
			throw IllegalStateException("Input jar and output jar are the same file; this breaks caching" + 
				"\nTo ignore this, set ignoreSameInputOutput to true")
		}
		process()
	}
	
	protected abstract fun process()
}