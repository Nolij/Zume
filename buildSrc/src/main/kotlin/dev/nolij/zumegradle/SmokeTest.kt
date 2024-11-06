@file:OptIn(ExperimentalPathApi::class)

package dev.nolij.zumegradle

import org.gradle.api.logging.Logger
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Files
import java.util.*
import kotlin.io.path.*

fun sleep(millis: Long) {
	Thread.sleep(millis)
}

class SmokeTest(
	private val logger: Logger,
	private val portableMCBinary: String,
	private val modFile: File,
	private val mainDir: String,
	private val workDir: String,
	private val maxThreads: Int,
	private val threadTimeout: Long,
	private val configs: List<Config>
) {

	data class Config(
		val modLoader: String,
		val mcVersion: String,
		val loaderVersion: String? = null,
		val jvmVersion: Int? = null,
		val extraArgs: List<String>? = null,
		val dependencies: List<Pair<String, String>>? = null,
	) {
		val versionString: String get() =
			if (loaderVersion != null)
				"${modLoader}:${mcVersion}:${loaderVersion}"
			else
				"${modLoader}:${mcVersion}"

		override fun toString(): String {
			val result = StringBuilder()

			result.appendLine("modLoader=${modLoader}")
			result.appendLine("mcVersion=${mcVersion}")
			result.appendLine("loaderVersion=${loaderVersion}")
			result.appendLine("jvmVersion=${jvmVersion}")
			result.appendLine("extraArgs=[${extraArgs?.joinToString(", ") ?: ""}]")
			result.appendLine("mods=[${dependencies?.joinToString(", ") { (name, _) -> name } ?: ""}]")

			return result.toString()
		}
	}
	
	private enum class ThreadState {
		PENDING,
		RUNNING,
		TIMED_OUT,
		READY,
		PASSED,
		FAILED,
	}

	private inner class Thread(val config: Config) {
		private val name: String = config.hashCode().toUInt().toString(16)
		private val instanceDir = "${workDir}/${name}"
		private val modsDir = "${instanceDir}/mods"
		private val logDir = "${instanceDir}/logs/latest.log"
		private val logFile = File(logDir)
		private val command: Array<String>

		private var process: Process? = null
		
		private var startTimestamp: Long? = null

		val isAlive: Boolean get() = process?.isAlive == true
		private val isTimedOut: Boolean get() = 
			if (isAlive)
				System.nanoTime() - startTimestamp!! > threadTimeout
			else
				false

		private var finalState: ThreadState? = null
		val finished: Boolean get() = finalState != null
		val state: ThreadState
			get() {
				return finalState ?: 
				if (startTimestamp == null) ThreadState.PENDING
				else if (isTimedOut) ThreadState.TIMED_OUT 
				else if (isAlive) ThreadState.RUNNING 
				else ThreadState.READY
			}

		init {
			Path(instanceDir).also { path ->
				if (!path.exists())
					path.createDirectories()
			}

			Path(modsDir).also { modsPath ->
				if (modsPath.exists())
					modsPath.deleteRecursively()
				modsPath.createDirectories()
			}

			Path(logDir).also { logPath ->
				logPath.deleteIfExists()
				logPath.parent.also { logsPath ->
					if (!logsPath.exists())
						logsPath.createDirectories()
				}
			}

			config.dependencies?.forEach { (name, urlString) ->
				URL(urlString).openStream().use { inputStream ->
					FileOutputStream("${modsDir}/${name}.jar").use(inputStream::transferTo)
				}
			}

			Files.copy(modFile.toPath(), Path("${modsDir}/${modFile.name}"))

			val extraArgs = arrayListOf<String>()

			val jvmVersionMap = mapOf(
				17 to "java-runtime-gamma",
				21 to "java-runtime-delta",
				8 to "jre-legacy"
			)
			if (config.jvmVersion != null)
				extraArgs.add("--jvm=${mainDir}/jvm/${jvmVersionMap[config.jvmVersion]!!}/bin/java")

			if (config.extraArgs != null)
				extraArgs.addAll(config.extraArgs)

			command = arrayOf(
				portableMCBinary,
				"--main-dir", mainDir,
				"--work-dir", instanceDir,
				"start", config.versionString,
				*extraArgs.toTypedArray(),
				"--jvm-args=-DzumeGradle.auditAndExit=true",
			)

			ProcessBuilder(*command, "--dry")
				.inheritIO()
				.start()
				.waitFor()
		}
		
		fun start() {
			if (state != ThreadState.PENDING)
				error("Thread already started!")
			
			startTimestamp = System.nanoTime()
			process = ProcessBuilder(*command)
				.inheritIO()
				.start()
		}

		fun update() {
			var passed = false

			when (state) {
				ThreadState.TIMED_OUT -> process!!.destroyForcibly()
				ThreadState.READY -> {
					if (logFile.exists()) {
						logFile.reader().use { reader ->
							reader.forEachLine { line ->
								if (line.endsWith("ZumeGradle audit passed"))
									passed = true
							}
						}
					}
				}
				else -> return
			}

			if (passed) {
				println("Smoke test passed for config:\n${config}")
				finalState = ThreadState.PASSED
			} else {
				logger.error("Smoke test failed for config:\n${config}")
				finalState = ThreadState.FAILED
			}
			
			printThreads()
		}
	}
	
	private val threads = ArrayList<Thread>()
	
	private fun printThreads() {
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
			threads.add(Thread(config))
		}

		printThreads()

		do {
			while (threads.count { thread -> thread.isAlive } < maxThreads)
				threads.firstOrNull { thread -> thread.state == ThreadState.PENDING }?.start() ?: break
			sleep(500L)
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