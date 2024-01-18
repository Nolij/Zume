operator fun String.invoke(): String {
	return (rootProject.properties[this] as String?)!!
}

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

	mappings {
		mojmap()
		parchment(mcVersion = "neoforge_minecraft_version"(), version = "neoforge_parchment_version"())
	}

	defaultRemapJar = true
}

dependencies {
	"minecraftLibraries"("blue.endless:jankson:${"jankson_version"()}")
	
	"minecraftLibraries"(project(":common"))
}

tasks.processResources {
	from("common/src/main/resources")

	filteringCharset = "UTF-8"
	
	filesMatching("META-INF/mods.toml") {
		expand(rootProject.properties)
	}
}