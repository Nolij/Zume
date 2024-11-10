package dev.nolij.zumegradle.task

import dev.nolij.zumegradle.JsonShrinkingType
import org.gradle.api.tasks.Input
import dev.nolij.zumegradle.entryprocessing.EntryProcessor
import dev.nolij.zumegradle.entryprocessing.EntryProcessors
import org.gradle.api.provider.ListProperty
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.Deflater

abstract class JarEntryModificationTask : ProcessJarTask() {
    @get:Input
    protected abstract val processors: ListProperty<EntryProcessor>

    fun process(processor: EntryProcessor) {
        processors.add(processor)
    }

    fun json(type: JsonShrinkingType?, shouldRun: (String) -> Boolean = { it.endsWith(".json") }) {
        processors.add(when(type) {
            null -> EntryProcessors.PASS
            JsonShrinkingType.MINIFY -> EntryProcessors.jsonMinifier(shouldRun)
            JsonShrinkingType.PRETTY_PRINT -> EntryProcessors.jsonPrettyPrinter(shouldRun)
        })
    }

    override fun process() {
        val contents = linkedMapOf<String, ByteArray>()
        JarFile(inputJar.get().asFile).use {
            it.entries().asIterator().forEach { entry ->
                if (!entry.isDirectory) {
                    contents[entry.name] = it.getInputStream(entry).readAllBytes()
                }
            }
        }

        JarOutputStream(archiveFile.get().asFile.outputStream()).use { out ->
            out.setLevel(Deflater.BEST_COMPRESSION)
            contents.forEach { var (name, bytes) = it

                processors.get().forEach { processor ->
                    bytes = processor(name, bytes)
                }

                out.putNextEntry(JarEntry(name))
                out.write(bytes)
                out.closeEntry()
            }
            out.finish()
        }
    }
}