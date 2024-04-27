operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

unimined.minecraft {
	combineWith(project(":api").sourceSets.main.get())

	version("lexforge16_minecraft_version"())

	runs {
		config("server") {
			disabled = true
		}
	}

	minecraftForge {
		loader("lexforge16_version"())
		mixinConfig("zume-lexforge16.mixins.json")
	}

	mappings {
		searge()
		mojmap()
		parchment(mcVersion = "lexforge16_minecraft_version"(), version = "lexforge16_parchment_version"())
	}

	defaultRemapJar = true
}

dependencies {
	"minecraftLibraries"("blue.endless:jankson:${"jankson_version"()}")
}