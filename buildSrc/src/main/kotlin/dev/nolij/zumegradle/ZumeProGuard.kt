package dev.nolij.zumegradle

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import proguard.gradle.ProGuardTask
import xyz.wagyourtail.unimined.api.minecraft.MinecraftConfig
import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask
import xyz.wagyourtail.unimined.api.unimined
import xyz.wagyourtail.unimined.util.sourceSets
import java.io.File

fun applyProGuard(task: ProGuardTask) {
	task.apply {
		outputs.upToDateWhen { false }
		val javaHome = System.getProperty("java.home")
		libraryjars(mapOf("jarfilter" to "!**.jar", "filter" to "!module-info.class"),
			arrayOf("$javaHome/jmods/java.base.jmod", "$javaHome/jmods/java.desktop.jmod"))
		
		dontwarn("java.lang.invoke.MethodHandle")
		allowaccessmodification()
		optimizationpasses(10) // 10 is a lot but if nothing happens after a pass, it will stop
		dontusemixedcaseclassnames()
		keepattributes("RuntimeVisibleAnnotations")
		overloadaggressively()
	}
}

class ZumeProGuard : Plugin<Project> {
	override fun apply(project: Project) {
		project.afterEvaluate {
			tasks.register<ProGuardTask>("proguard") {
				group = "build"
				applyProGuard(this)

				val jar = tasks.withType<RemapJarTask>()["remapJar"]
				val jarchive = jar.archiveFile.get().asFile
				dependsOn(jar)

				injars(jarchive)
				val outFile = jar.destinationDirectory.get().asFile
					.resolve("${jarchive.nameWithoutExtension}-proguard.jar")
				outjars(outFile)

				val filter = mapOf(
					"jarfilter" to "!**.jar",
					"filter" to "!module-info.class"
				)

				libraryjars(filter, getUnmappedMinecraftJar().absolutePath)
				listOf("compileClasspath", "minecraftLibraries").forEach {
					configurations[it].forEach fe@ {
						if (!it.name.endsWith(".jar")) return@fe
						libraryjars(filter, it.absolutePath)
					}
				}
				
				keep("class dev.nolij.zume.api.** { *; }")
				keep("class dev.nolij.zume.mixin.** { @org.spongepowered.asm.mixin.** <methods>; }")
				keep("class ** implements net.fabricmc.api.ClientModInitializer { void onInitializeClient(); }")
				keep("@net.minecraftforge.fml.common.Mod class * { *; }")
				keep("@net.neoforged.fml.common.Mod class * { *; }")
				
				printmapping(layout.buildDirectory.dir("proguard").get().file("mapping.txt").asFile.apply { 
					parentFile.mkdirs()
					if(exists()) delete()
					createNewFile()
				})
				repackageclasses("dev.nolij.zume")
			}.get()
		}
	}
	
	private fun Project.getUnmappedMinecraftJar(): File {
		val mcc = unimined.minecrafts[sourceSets["main"]]
		return mcc.getMinecraft(mcc.mcPatcher.prodNamespace, mcc.mcPatcher.prodNamespace).toFile()
	}
	
	private val Dependency.value: String
		get() = "${group}:${name}:${version}"
}