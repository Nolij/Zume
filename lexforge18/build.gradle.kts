operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

unimined.minecraft {
	version("lexforge18_minecraft_version"())

	minecraftForge {
		loader("lexforge18_version"())
		mixinConfig("zume-lexforge18.mixins.json")
	}

	mappings {
		mojmap()
		parchment(mcVersion = "lexforge18_minecraft_version"(), version = "lexforge18_parchment_version"())
	}
}

dependencies {
	compileOnly(project(":stubs"))

	"minecraftLibraries"("dev.nolij:zson:${"zson_version"()}")
}