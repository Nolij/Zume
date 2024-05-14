operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

unimined.minecraft {
	version("lexforge16_minecraft_version"())

	minecraftForge {
		loader("lexforge16_version"())
		mixinConfig("zume-lexforge16.mixins.json")
	}

	mappings {
		searge()
		mojmap()
		parchment(mcVersion = "lexforge16_minecraft_version"(), version = "lexforge16_parchment_version"())
	}
}

dependencies {
	"minecraftLibraries"("dev.nolij:zson:${"zson_version"()}")
}