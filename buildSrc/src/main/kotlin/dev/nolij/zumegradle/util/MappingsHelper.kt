package dev.nolij.zumegradle.util

import net.fabricmc.mappingio.MappingReader
import net.fabricmc.mappingio.format.MappingFormat
import net.fabricmc.mappingio.tree.MappingTree.ClassMapping
import net.fabricmc.mappingio.tree.MemoryMappingTree
import java.io.File

fun mappings(file: File, format: MappingFormat = MappingFormat.PROGUARD): MemoryMappingTree {
	if (!file.exists()) {
		error("Mappings file $file does not exist")
	}

	val mappingTree = MemoryMappingTree()
	MappingReader.read(file.toPath(), format, mappingTree)
	return mappingTree
}

@Suppress("INACCESSIBLE_TYPE", "NAME_SHADOWING")
fun MemoryMappingTree.map(src: String): String {
	val src = src.replace('.', '/')
	val dstNamespaceIndex = getNamespaceId(dstNamespaces[0])
	val classMapping: ClassMapping? = getClass(src)
	if (classMapping == null) {
		println("Class $src not found in mappings")
		return src
	}
	return classMapping.getDstName(dstNamespaceIndex).replace('/', '.')
}