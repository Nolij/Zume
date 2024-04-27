operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

unimined.minecraft {
	combineWith(project(":api").sourceSets.main.get())
	
	version("lexforge_minecraft_version"())

	runs {
		config("server") {
			disabled = true
		}
	}

	minecraftForge {
		loader("lexforge_version"())
		mixinConfig("zume-lexforge.mixins.json")
	}

	mappings {
		mojmap()
		parchment(mcVersion = "lexforge_minecraft_version"(), version = "lexforge_parchment_version"())
	}

	defaultRemapJar = true
}

dependencies {
	compileOnly(project(":stubs"))

	"minecraftLibraries"("blue.endless:jankson:${"jankson_version"()}")
}