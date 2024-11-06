package dev.nolij.zumegradle.smoketest

import org.gradle.api.logging.Logger
import java.io.File
import java.util.*

import dev.nolij.zumegradle.smoketest.Thread.ThreadState
import org.gradle.api.Project

class SmokeTest(
	internal val project: Project,
	internal val portableMCBinary: String,
	internal val modFile: File,
	internal val mainDir: String,
	internal val workDir: String,
	private val maxThreads: Int,
	internal val threadTimeout: Long,
	private val configs: List<Config>
) {
	
	internal val logger: Logger = project.logger
	
	private val threads = ArrayList<Thread>()
	
	internal fun printThreads() {
		println("""
				>   TOTAL: ${threads.filter { thread -> thread.finished }.size}/${configs.size}
				> RUNNING: ${threads.filter { thread -> thread.state == ThreadState.RUNNING }.size}/${maxThreads}
				>  PASSED: ${threads.filter { thread -> thread.state == ThreadState.PASSED }.size}
				>  FAILED: ${threads.filter { thread -> thread.state == ThreadState.FAILED }.size}
			""".trimIndent())
	}
	
	fun test() {
		println("Setting up instances...")
		configs.forEach { config ->			
			threads.add(Thread(this, config))
		}

		printThreads()

		do {
			while (threads.count { thread -> thread.isAlive } < maxThreads)
				threads.firstOrNull { thread -> thread.state == ThreadState.PENDING }?.start() ?: break
			java.lang.Thread.sleep(500L)
			threads.forEach(Thread::update)
		} while (threads.any { thread -> !thread.finished })

		val failedConfigs = threads
			.filter { thread -> thread.state == ThreadState.FAILED }
			.map { thread -> thread.config }
		
		if (failedConfigs.isNotEmpty()) {
			logger.error("[{\n${failedConfigs.joinToString("}, {\n")}}]")
			error("One or more tests failed. See logs for more details.")
		}

		println("All tests passed.")
	}
	
}