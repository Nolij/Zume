operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

unimined.minecraft {
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
}

dependencies {
	compileOnly(project(":stubs"))
	
	"minecraftLibraries"("dev.nolij:zson:${"zson_version"()}")
}