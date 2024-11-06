package dev.nolij.zumegradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.jvm.tasks.Jar
import org.gradle.api.tasks.TaskAction

import dev.nolij.zumegradle.smoketest.Config
import dev.nolij.zumegradle.smoketest.SmokeTest

abstract class SmokeTestTask : DefaultTask() {
	init {
		group = "verification"
	}

	/**
	 * The task that provides the input jar file.
	 */
	@get:Input
	abstract val inputTask: Property<Jar>
	
	@get:InputDirectory
	abstract val mainDir: RegularFileProperty
	
	@get:InputDirectory
	abstract val workDir: RegularFileProperty
	
	@get:Input
	abstract val maxThreads: Property<Int>
	
	@get:Input
	abstract val threadTimeout: Property<Long>
	
	@get:Input
	abstract val configs: ListProperty<Config>
	
	@get:Input
	abstract val portableMCBinary: Property<String>
	
	fun config(config: Config) {
		configs.add(config)
	}
	
	fun configs(vararg configs: Config) {
		this.configs.addAll(configs.asList())
	}
	
	@TaskAction
	fun runSmokeTest() {
		SmokeTest(
			logger = logger,
			portableMCBinary = portableMCBinary.get(),
			modFile = inputTask.get().archiveFile.get().asFile,
			mainDir = mainDir.get().asFile.absolutePath,
			workDir = workDir.get().asFile.absolutePath,
			maxThreads = maxThreads.get(),
			threadTimeout = threadTimeout.get(),
			configs = configs.get()
		).test()
	}
}