@file:OptIn(ExperimentalPathApi::class)

package dev.nolij.zumegradle

import org.gradle.api.logging.Logger
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Files
import java.util.*
import kotlin.io.path.*
import kotlin.math.max

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
		val name: String = hashCode().toUInt().toString(16)
		
		val versionString: String get() =
			if (loaderVersion != null)
				"${modLoader}:${mcVersion}:${loaderVersion}"
			else
				"${modLoader}:${mcVersion}"

		override fun toString(): String {
			val result = StringBuilder()

			result.appendLine("name=${name}")
			result.appendLine("modLoader=${modLoader}")
			result.appendLine("mcVersion=${mcVersion}")
			result.appendLine("loaderVersion=${loaderVersion}")
			result.appendLine("jvmVersion=${jvmVersion}")
			result.appendLine("extraArgs=[${extraArgs?.joinToString(", ") ?: ""}]")
			result.appendLine("mods=[${dependencies?.joinToString(", ") { (name, _) -> name } ?: ""}]")

			return result.toString()
		}
	}
	
	private enum class ThreadStage {
		SETUP,
		TESTING,
		COMPLETED,
	}
	
	private enum class FailureReason {
		SETUP_NONZERO_EXIT_CODE,
		TIMED_OUT,
		TESTING_NONZERO_EXIT_CODE,
		SUCCESS_LOG_MISSING,
	}

	private inner class Thread(val config: Config) {
		val instancePath = "${workDir}/${config.name}"
		val modsPath = "${instancePath}/mods"
		val command: Array<String>
		val setupLogFile = File("${instancePath}/setup.log")
		val testLogFile = File("${instancePath}/test.log")
		val gameLogFile = File("${instancePath}/logs/latest.log")

		private var process: Process? = null
		private var startTimestamp: Long? = null

		val alive: Boolean get() = process?.isAlive == true
		private val isTimedOut: Boolean get() = 
			if (alive)
				System.nanoTime() - startTimestamp!! > threadTimeout
			else
				false
		
		var stage: ThreadStage = ThreadStage.SETUP
			private set
		var failureReason: FailureReason? = null
			private set
		val failed: Boolean get() = failureReason != null

		val ready: Boolean get() = !failed && stage == ThreadStage.SETUP
		val done: Boolean get() = failed || stage == ThreadStage.COMPLETED

		init {
			Path(instancePath).also { path ->
				if (!path.exists())
					path.createDirectories()
			}

			Path(modsPath).also { modsPath ->
				if (modsPath.exists())
					modsPath.deleteRecursively()
				modsPath.createDirectories()
			}
			
			if (gameLogFile.exists())
				gameLogFile.delete()

			config.dependencies?.forEach { (name, urlString) ->
				URL(urlString).openStream().use { inputStream ->
					FileOutputStream("${modsPath}/${name}.jar").use(inputStream::transferTo)
				}
			}

			Files.copy(modFile.toPath(), Path("${modsPath}/${modFile.name}"))

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
				"--work-dir", instancePath,
				"start", config.versionString,
				*extraArgs.toTypedArray(),
				"--jvm-args=-DzumeGradle.auditAndExit=true -Xmx1G",
			)

			if (ProcessBuilder(*command, "--dry")
				.redirectOutput(setupLogFile)
				.start()
				.waitFor() != 0) {
				failureReason = FailureReason.SETUP_NONZERO_EXIT_CODE
			}
		}
		
		private fun start() {
			if (stage != ThreadStage.SETUP)
				error("Thread already started")
			else if (failed)
				error("Cannot start thread which failed setup")
			
			stage = ThreadStage.TESTING
			
			startTimestamp = System.nanoTime()
			process = ProcessBuilder(*command)
				.redirectOutput(testLogFile)
				.start()
		}

		fun step() {
			var passed = false

			if (stage == ThreadStage.SETUP && ready && anyAvailableThreads) {
				return start()
			} else if (stage == ThreadStage.TESTING && isTimedOut) {
				failureReason = FailureReason.TIMED_OUT
				process!!.destroyForcibly()
			} else if (stage == ThreadStage.TESTING && !alive) {
				if (process!!.exitValue() == 0) {
					(if (gameLogFile.exists()) gameLogFile else testLogFile).reader().use { reader ->
						reader.forEachLine { line ->
							if (line.endsWith("ZumeGradle audit passed")) {
								passed = true
							}
						}
					}
					
					if (!passed) {
						failureReason = FailureReason.SUCCESS_LOG_MISSING
					}
				} else {
					failureReason = FailureReason.TESTING_NONZERO_EXIT_CODE
				}
			} else {
				return
			}
			
			stage = ThreadStage.COMPLETED

			if (passed) {
				println("Smoke test passed for config:\n${config}")
			} else {
				logger.error("Smoke test failed for config:\n${config}")
			}
			
			printThreads()
		}
	}
	
	private val threads = ArrayList<Thread>()
	private val runningThreads: List<Thread> get() = threads.filter(Thread::alive)
	private val pendingThreads: List<Thread> get() = threads.filter { thread -> !thread.done }
	private val finishedThreads: List<Thread> get() = threads.filter(Thread::done)
	private val passedThreads: List<Thread> get() = finishedThreads.filter { thread -> !thread.failed }
	private val failedThreads: List<Thread> get() = threads.filter(Thread::failed)
	private val availableThreads: Int get() = max(0, maxThreads - runningThreads.size)
	private val anyAvailableThreads: Boolean get() = runningThreads.size < maxThreads
	
	private fun printThreads() {
		println("""
				>   TOTAL: ${finishedThreads.size}/${configs.size}
				> RUNNING: ${runningThreads.size}/${maxThreads}
				>  PASSED: ${passedThreads.size}
				>  FAILED: ${failedThreads.size}
			""".trimIndent())
	}
	
	fun test() {
		println("Setting up instances...")
		configs.forEach { config ->			
			threads.add(Thread(config))
		}

		printThreads()

		do {
			threads.forEach(Thread::step)
			sleep(500L)
		} while (threads.any { thread -> !thread.done })
		
		if (failedThreads.isNotEmpty()) {
			failedThreads.forEach { thread ->
				logger.error(
					"Config ${thread.config.name} failed!\n" + 
					"> STAGE: ${thread.stage}\n" + 
					"> CONFIG: {\n${thread.config}}\n" +
					"> COMMAND: [${thread.command.joinToString(", ")}]\n" +
					"> FAILURE REASON: ${thread.failureReason}\n" + 		
					"> INSTANCE PATH: ${thread.instancePath}\n"
				)
			}
			error("${failedThreads.size} smoke test config(s) failed. See logs for more details.")
		}

		println("All tests passed.")
	}
	
}