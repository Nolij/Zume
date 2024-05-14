operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

unimined.minecraft {
	version("lexforge_minecraft_version"())

	minecraftForge {
		loader("lexforge_version"())
		mixinConfig("zume-lexforge.mixins.json")
	}

	mappings {
		mojmap()
		parchment(mcVersion = "lexforge_minecraft_version"(), version = "lexforge_parchment_version"())
	}
}

dependencies {
	compileOnly(project(":stubs"))

	"minecraftLibraries"("dev.nolij:zson:${"zson_version"()}")
}