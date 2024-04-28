package dev.nolij.zumegradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import proguard.gradle.ProGuardTask
import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask
import xyz.wagyourtail.unimined.api.unimined
import xyz.wagyourtail.unimined.util.sourceSets

val filter = mapOf(
	"jarfilter" to "!**.jar",
	"filter" to "!module-info.class"
)

class ZumeProGuard : Plugin<Project> {
	override fun apply(project: Project) {
		(project as Any).apply {
			this as Project
			val proguard = tasks.register<ProGuardTask>("proguard") {
				group = "build"
				outputs.upToDateWhen { false }
				val javaHome = System.getProperty("java.home")
				
				dontwarn("java.lang.invoke.MethodHandle")
				allowaccessmodification()
				optimizationpasses(10) // 10 is a lot but if nothing happens after a pass, it will stop
				dontusemixedcaseclassnames()
				keepattributes("RuntimeVisibleAnnotations")
				overloadaggressively()
				
				// add the jdk (base and desktop) to the libraries
				libraryjars(filter, arrayOf("$javaHome/jmods/java.base.jmod", "$javaHome/jmods/java.desktop.jmod"))
				
				keep("class dev.nolij.zume.mixin.** { @org.spongepowered.asm.mixin.** <methods>; }")
				keep("class ** implements net.fabricmc.api.ClientModInitializer { void onInitializeClient(); }")
				keep("@net.minecraftforge.fml.common.Mod class ** { *; }")
				keep("@net.neoforged.fml.common.Mod class ** { *; }")
				
				printmapping(layout.buildDirectory.dir("proguard").get().file("mapping.txt").asFile.apply { 
					parentFile.mkdirs()
					if(exists()) delete()
					createNewFile()
				})
				repackageclasses("dev.nolij.zume.${project.name}")
			}.get()
			
			afterEvaluate { 
				proguard.apply {
					val minecraft = unimined.minecrafts[sourceSets["main"]]
					val prodNamespace = minecraft.mcPatcher.prodNamespace

					// add obfuscated minecraft
					libraryjars(filter,
						minecraft.getMinecraft(prodNamespace, prodNamespace).toFile().absolutePath)

					// add dependencies
					libraryjars(filter,
						minecraft.mods.getClasspathAs(prodNamespace, prodNamespace,
							minecraft.sourceSet.compileClasspath.files.filter { !minecraft.isMinecraftJar(it.toPath()) }.toSet()
						))
					
					val jar = tasks.withType<RemapJarTask>()["remapJar"]
					val jarchive = jar.archiveFile.get().asFile
					dependsOn(jar)

					injars(jarchive)
					val outFile = jar.destinationDirectory.get().asFile
						.resolve("${jarchive.nameWithoutExtension}-proguard.jar")
					outjars(outFile)
				}
			}
		}
	}
}