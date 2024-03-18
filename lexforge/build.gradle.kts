operator fun String.invoke(): String {
	return (rootProject.properties[this] as String?)!!
}

unimined.minecraft {
	combineWith(project(":common").sourceSets.main.get())
	
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