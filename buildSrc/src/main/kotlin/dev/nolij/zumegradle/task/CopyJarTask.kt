package dev.nolij.zumegradle.task

abstract class CopyJarTask : ProcessJarTask() {
	override fun process() {
		inputJar.get().asFile.copyTo(archiveFile.get().asFile, true)
	}
}