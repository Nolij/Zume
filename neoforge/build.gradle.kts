operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

val modCompileOnly: Configuration by configurations.creating {
	configurations.compileClasspath.get().extendsFrom(this)
}
val modRuntimeOnly: Configuration by configurations.creating {
	configurations.runtimeClasspath.get().extendsFrom(this)
}
val mod: Configuration by configurations.creating {
	configurations.compileClasspath.get().extendsFrom(this)
	configurations.runtimeClasspath.get().extendsFrom(this)
}

unimined.minecraft {
	version("neoforge_minecraft_version"())
	
	neoForge {
		loader("neoforge_version"())
	}

	source {
		sourceGenerator.jvmArgs = listOf("-Xmx4G")
	}

	mappings {
		mojmap()
//		parchment(mcVersion = "neoforge_minecraft_version"(), version = "neoforge_parchment_version"())
	}

	mods {
		remap(modCompileOnly)
		remap(modRuntimeOnly)
		remap(mod)
	}
}

repositories {
	maven("https://maven.blamejared.com")
}

dependencies {
	compileOnly(project(":stubs"))
	
	modCompileOnly("org.embeddedt:embeddium-1.21:${"embeddium_neoforge_version"()}:api")
	modRuntimeOnly("org.embeddedt:embeddium-1.21:${"embeddium_neoforge_version"()}") {
		isTransitive = false
	}
}