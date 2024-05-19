operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

val modRuntimeOnly: Configuration by configurations.creating {
	configurations.runtimeClasspath.get().extendsFrom(this)
}

unimined.minecraft {
	combineWith(project(":integration:embeddium").sourceSets.main.get())
	
	version("neoforge_minecraft_version"())

	neoForged {
		loader("neoforge_version"())
	}

	source {
		sourceGenerator.jvmArgs = listOf("-Xmx4G")
	}

	mappings {
		mojmap()
		parchment(mcVersion = "neoforge_minecraft_version"(), version = "neoforge_parchment_version"())
	}

	mods {
		remap(modRuntimeOnly)
	}
}

repositories {
	maven("https://maven.blamejared.com")
}

dependencies {
	compileOnly(project(":stubs"))
	
	"minecraftLibraries"("dev.nolij:zson:${"zson_version"()}")

	modRuntimeOnly("org.embeddedt:embeddium-1.20.6:${"embeddium_neoforge_version"()}")
}