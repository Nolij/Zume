import proguard.gradle.ProGuardTask

plugins {
	id("com.github.gmazzo.buildconfig")
	id("com.github.johnrengelman.shadow")
}

operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

buildConfig {
	className("ZumeConstants")
	packageName("dev.nolij.zume.impl")

	useJavaOutput()

	// the below errors shown by IntelliJ can be safely ignored; Jabel works around this
	buildConfigField("MOD_ID", "mod_id"())
	buildConfigField("MOD_VERSION", Zume.version)
	buildConfigField("MOD_NAME", "mod_name"())
	buildConfigField("ARCHAIC_VERSION_RANGE", "archaic_minecraft_range"())
	buildConfigField("VINTAGE_VERSION_RANGE", "vintage_minecraft_range"())
}

repositories {
	maven("https://repo.spongepowered.org/repository/maven-public/")
}

val shade: Configuration by configurations.creating {
	configurations.compileClasspath.get().extendsFrom(this)
	configurations.runtimeClasspath.get().extendsFrom(this)
}

dependencies {
	compileOnly("org.apache.logging.log4j:log4j-core:${"log4j_version"()}")
	
	compileOnly("org.ow2.asm:asm-tree:${"asm_version"()}")
	compileOnly("org.spongepowered:mixin:${"mixin_version"()}")
	
	shade("blue.endless:jankson:${"jankson_version"()}")
}

val proguard = tasks.register<ProGuardTask>("proguardInternal") {
	group = "internal"
	outputs.upToDateWhen { false }
	
	val jar = tasks.shadowJar.get().archiveFile.get().asFile
	dependsOn(tasks.shadowJar)
	
	injars(jar.absolutePath)
	outjars(jar.absolutePath)
	
	dontwarn("java.lang.invoke.MethodHandle")
	allowaccessmodification()
	optimizationpasses(10) // 10 is a lot but if nothing happens after a pass, it will stop
	dontusemixedcaseclassnames()
	keepattributes("RuntimeVisibleAnnotations")
	overloadaggressively()

	val javaHome = System.getProperty("java.home")
	// add the jdk (base and desktop) to the libraries
	libraryjars(mapOf(
		"jarfilter" to "!**.jar",
		"filter" to "!module-info.class"
	), arrayOf("$javaHome/jmods/java.base.jmod", "$javaHome/jmods/java.desktop.jmod"))
	
	configurations.compileClasspath.get().forEach { file ->
		libraryjars(mapOf(
			"jarfilter" to "!**.jar",
			"filter" to "!module-info.class"
		), file.absolutePath)
	}
	
	keep("class dev.nolij.zume.api.** { *; }")
	keepclassmembers("class ** { @blue.endless.jankson.Comment <fields>; }")
	keep("class dev.nolij.zume.ZumeMixinPlugin { *; }")
	
	printmapping(layout.buildDirectory.dir("proguard").get().file("mapping.txt").asFile.apply { 
		parentFile.mkdirs()
		if(exists()) delete()
		createNewFile()
	})
	repackageclasses("dev.nolij.zume")
}

tasks.register<Jar>("proguard") {
	group = "build"
	dependsOn(proguard)
	(archiveFile as RegularFileProperty).set(proguard.get().outJarFiles.single() as File)
}

tasks.processResources {
	from("src/main/resources/assets/zume/lang/") {
		include("*.lang")
		rename { name -> name.lowercase() }
		into("assets/zume/lang/")
	}

	from("src/main/resources/assets/zume/lang/") {
		include("*.lang")
		into("assets/zume/stationapi/lang/")
	}
}