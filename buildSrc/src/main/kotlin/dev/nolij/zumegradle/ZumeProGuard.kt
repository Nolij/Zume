package dev.nolij.zumegradle

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import proguard.gradle.ProGuardTask

class ZumeProGuard : Plugin<Project> {
	override fun apply(project: Project) {
		if(project.rootProject != project) {
			throw IllegalStateException("This plugin must be applied to the root project")
		}
		
		project.tasks.register<ProGuardTask>("proguard") {
			configuration("proguard.pro")
			group = "build"
			outputs.upToDateWhen { false }
			val compressJar = project.tasks.withType<CompressJarTask>().single()
			dependsOn(compressJar)
			
			injars(compressJar.outputJar)
			val outFile = project.tasks.withType<ShadowJar>()["shadowJar"].destinationDirectory.get()
				.asFile.resolve("${project.extra["mod_id"]}-${project.version}-proguard.jar")
			outjars(outFile)
			
			project.subprojects.forEach { subproject ->
				subproject.configurations["compileClasspath"].forEach {
					if(it.name.endsWith(".jar")) {
						this@register.libraryjars(
							mapOf("jarfilter" to "!**.jar",
								"filter"    to "!module-info.class"),
							it.absolutePath
						)
					}
				}
			}

			val javaHome = System.getProperty("java.home")
			libraryjars(
				// filters must be specified first, as a map
				mapOf("jarfilter" to "!**.jar",
					"filter"    to "!module-info.class"),
				"$javaHome/jmods/java.base.jmod"
			)
		}
	}
}