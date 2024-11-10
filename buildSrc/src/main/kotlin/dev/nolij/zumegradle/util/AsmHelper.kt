package dev.nolij.zumegradle.util

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode

fun ClassNode.toBytes(flags: Int = 0): ByteArray {
	val writer = ClassWriter(flags)
	this.accept(writer)
	return writer.toByteArray()
}

fun ClassNode(bytes: ByteArray): ClassNode {
	val node = ClassNode()
	ClassReader(bytes).accept(node, 0)
	return node
}