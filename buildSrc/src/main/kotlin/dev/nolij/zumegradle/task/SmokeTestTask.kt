package dev.nolij.zumegradle.task

import dev.nolij.zumegradle.smoketest.SmokeTest
import dev.nolij.zumegradle.smoketest.SmokeTest.Config
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.jvm.tasks.Jar
import org.gradle.api.tasks.TaskAction

abstract class SmokeTestTask : DefaultTask() {
	init {
		group = "verification"
	}

	/**
	 * The task that provides the input jar file.
	 */
	@get:Input
	abstract val inputTask: Property<Jar>
	
	@get:Input
	abstract val mainDir: Property<String>
	
	@get:Input
	abstract val workDir: Property<String>
	
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
			project,
			portableMCBinary.get(),
			inputTask.get().archiveFile.get().asFile,
			mainDir.get(),
			workDir.get(),
			maxThreads.get(),
			threadTimeout.get(),
			configs.get()
		).test()
	}
}