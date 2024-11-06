package dev.nolij.zumegradle.smoketest

import java.io.File
import java.nio.file.Files
import kotlin.io.path.*

@OptIn(ExperimentalPathApi::class)
internal class Thread(
	private val smokeTest: SmokeTest,
	val config: Config
) {
	private val name: String = config.hashCode().toUInt().toString(16)
	private val instanceDir = "${smokeTest.workDir}/${name}"
	private val modsDir = "${instanceDir}/mods"
	private val logDir = "${instanceDir}/logs/latest.log"
	private val logFile = File(logDir)
	private val command: Array<String>

	private var process: Process? = null

	private var startTimestamp: Long? = null

	val isAlive: Boolean get() = process?.isAlive == true
	private val isTimedOut: Boolean get() =
		if (isAlive)
			System.nanoTime() - startTimestamp!! > smokeTest.threadTimeout
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
		
		if(config.dependencies != null) {
			val files = smokeTest.project.configurations.detachedConfiguration(
				*config.dependencies.map {
					smokeTest.project.dependencies.create(it)
				}.toTypedArray()
			).resolve()
			files.forEach { file ->
				Files.copy(file.toPath(), Path("${modsDir}/${file.name}"))
			}
		}
		

		Files.copy(smokeTest.modFile.toPath(), Path("${modsDir}/${smokeTest.modFile.name}"))

		val extraArgs = arrayListOf<String>()

		val jvmVersionMap = mapOf(
			17 to "java-runtime-gamma",
			21 to "java-runtime-delta",
			8 to "jre-legacy"
		)
		if (config.jvmVersion != null)
			extraArgs.add("--jvm=${smokeTest.mainDir}/jvm/${jvmVersionMap[config.jvmVersion]!!}/bin/java")

		if (config.extraArgs != null)
			extraArgs.addAll(config.extraArgs)

		command = arrayOf(
			smokeTest.portableMCBinary,
			"--main-dir", smokeTest.mainDir,
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
			smokeTest.logger.error("Smoke test failed for config:\n${config}")
			finalState = ThreadState.FAILED
		}

		smokeTest.printThreads()
	}

	internal enum class ThreadState {
		PENDING,
		RUNNING,
		TIMED_OUT,
		READY,
		PASSED,
		FAILED,
	}
}