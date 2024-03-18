operator fun String.invoke(): String {
	return (rootProject.properties[this] as String?)!!
}

unimined.minecraft {
	combineWith(project(":common").sourceSets.main.get())

	version("lexforge18_minecraft_version"())

	runs {
		config("server") {
			disabled = true
		}
	}

	minecraftForge {
		loader("lexforge18_version"())
		mixinConfig("zume-lexforge18.mixins.json")
	}

	mappings {
		mojmap()
		parchment(mcVersion = "lexforge18_minecraft_version"(), version = "lexforge18_parchment_version"())
	}

	defaultRemapJar = true
}

dependencies {
	compileOnly(project(":stubs"))

	"minecraftLibraries"("blue.endless:jankson:${"jankson_version"()}")
}