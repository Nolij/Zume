package dev.nolij.zumegradle.task

import dev.nolij.zumegradle.task.ConstantPool.Companion.toConstantPool
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.DataInputStream
import java.io.File

@CacheableTask
abstract class JvmdgStubCheckTask : DefaultTask() {
	init {
		group = "verification"
		description = "Checks if jvmdg stubs are present in any classes"
	}
	
	@get:InputFiles
	@get:Classpath
	abstract val classes: Property<FileCollection>

	/**
	 * Any stubs that are allowed to be present in the classes
	 */
	@get:Input
	abstract val allowedStubs: SetProperty<String>
	
	fun classesRoot(dir: DirectoryProperty) {
		classes.set(dir.asFileTree.matching { include("**/*.class") })
	}
	
	@TaskAction
	fun checkStubs() {
		val allowedStubs = allowedStubs.get()
		val classes = classes.get().filter { it.isFile }
		
		val allCPEntries = classes.map { getClassInfos(it) }
		allCPEntries.forEach { 
			println("class: ${it.first}")
			it.second.forEach { println("  $it") }
		}
			
		val stubs = allCPEntries
			.map { (className, stubs) -> 
				className to stubs.filter { it.startsWith("xyz/wagyourtail/jvmdg/j") && it !in allowedStubs }
			}
			.filter { (_, stubs) -> stubs.isNotEmpty() }
		
		if (stubs.isNotEmpty()) {
			val message = buildString {
				append("Found JVMDowngrader stubs in classes:\n")
				for ((className, stubs) in stubs) {
					append("  $className:\n")
					for (stub in stubs) {
						append("    $stub\n")
					}
				}
			}
			throw IllegalArgumentException(message)
		}
	}
	
	private fun getClassInfos(file: File): Pair<String, Set<String>> {
		val d = DataInputStream(file.inputStream())
		val magic = d.readInt().toUInt()
		if (magic != 0xCAFEBABEu) {
			throw IllegalArgumentException("Not a class file; magic number is 0x${magic.toString(16)} (file: ${file.name})")
		}
		
		d.readInt() // minor + major version
		
		val constantPoolCount = d.readUnsignedShort()
		val constantPool = buildList {
			while (size + 1 < constantPoolCount) {
				val entry = ConstantPool.readEntry(d)
				if(entry is Invalid) {
					error("Unknown constant pool tag ${entry.tag} at index ${size + 1}")
				}
				
				add(entry)
				if (entry is LongInfo || entry is DoubleInfo) {
					// these take up 2 slots for some reason
					add(Invalid(-1))
				}
			}
		}.toConstantPool()
		
		d.readUnsignedShort() // access flags
		val thisClass = d.readUnsignedShort()
		val className = constantPool.get<Utf8Info>(constantPool.get<ClassInfo>(thisClass).nameIndex).value
		
		val stubs = constantPool.entries
			.filterIsInstance<ClassInfo>()
			.map { constantPool.get<Utf8Info>(it.nameIndex).value }
			.toSet()
		
		return className to stubs
	}
}

class ConstantPool(val entries: Array<ConstantPoolEntry>) {
	inline operator fun <reified T : ConstantPoolEntry> get(index: Int): T {
		val entry = entries[index - 1]
		if (entry !is T) {
			throw IllegalArgumentException("Expected ${T::class.simpleName}, got ${entry::class.simpleName}")
		}
		return entry
	}
	
	companion object {
		fun readEntry(d: DataInputStream): ConstantPoolEntry {
			val tag = d.readUnsignedByte()
			return when (tag) {
				7 -> ClassInfo(d.readUnsignedShort())
				9 -> FieldRef(d.readUnsignedShort(), d.readUnsignedShort())
				10 -> MethodRef(d.readUnsignedShort(), d.readUnsignedShort())
				11 -> InterfaceMethodRef(d.readUnsignedShort(), d.readUnsignedShort())
				8 -> StringInfo(d.readUnsignedShort())
				3 -> IntegerInfo(d.readInt())
				4 -> FloatInfo(d.readFloat())
				5 -> LongInfo(d.readLong())
				6 -> DoubleInfo(d.readDouble())
				12 -> NameAndType(d.readUnsignedShort(), d.readUnsignedShort())
				1 -> Utf8Info(d.readUTF())
				15 -> MethodHandle(d.readUnsignedByte(), d.readUnsignedShort())
				16 -> MethodType(d.readUnsignedShort())
				18 -> InvokeDynamic(d.readUnsignedShort(), d.readUnsignedShort())
				else -> Invalid(tag)
			}
		}

		fun Collection<ConstantPoolEntry>.toConstantPool() = ConstantPool(this.toTypedArray())
	}
}

sealed interface ConstantPoolEntry
data class ClassInfo(val nameIndex: Int) : ConstantPoolEntry
data class FieldRef(val classIndex: Int, val nameAndTypeIndex: Int) : ConstantPoolEntry
data class MethodRef(val classIndex: Int, val nameAndTypeIndex: Int) : ConstantPoolEntry
data class InterfaceMethodRef(val classIndex: Int, val nameAndTypeIndex: Int) : ConstantPoolEntry
data class StringInfo(val stringIndex: Int) : ConstantPoolEntry
data class IntegerInfo(val value: Int) : ConstantPoolEntry
data class FloatInfo(val value: Float) : ConstantPoolEntry
data class LongInfo(val value: Long) : ConstantPoolEntry
data class DoubleInfo(val value: Double) : ConstantPoolEntry
data class NameAndType(val nameIndex: Int, val descriptorIndex: Int) : ConstantPoolEntry
data class Utf8Info(val value: String) : ConstantPoolEntry
data class MethodHandle(val referenceKind: Int, val referenceIndex: Int) : ConstantPoolEntry
data class MethodType(val descriptorIndex: Int) : ConstantPoolEntry
data class InvokeDynamic(val bootstrapMethodAttrIndex: Int, val nameAndTypeIndex: Int) : ConstantPoolEntry
data class Invalid(val tag: Int) : ConstantPoolEntry