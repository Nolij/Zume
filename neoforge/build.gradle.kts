operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

unimined.minecraft {
	version("neoforge_minecraft_version"())

	runs {
		config("server") {
			disabled = true
		}
	}

	neoForged {
		loader("neoforge_version"())
	}

	source {
		sourceGenerator.jvmArgs = listOf("-Xmx4G")
	}

	mappings {
		mojmap()
//		parchment(mcVersion = "neoforge_minecraft_version"(), version = "neoforge_parchment_version"())
	}

	defaultRemapJar = true
}

dependencies {
	compileOnly(project(":stubs"))
	
	"minecraftLibraries"("blue.endless:jankson:${"jankson_version"()}")
	
	"minecraftLibraries"(project(":api"))
}