import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask

operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

unimined.minecraft {
	version("archaic_minecraft_version"())
	
	minecraftForge {
		loader("archaic_forge_version"())
		mixinConfig("zume-${project.name}.mixins.json")
	}
	
	mappings {
		searge()
		mcp("stable", "archaic_mappings_version"())
	}
}

dependencies {
	compileOnly(project(":stubs"))
	
	"modImplementation"("com.github.LegacyModdingMC.UniMixins:unimixins-all-1.7.10:${"unimixins_version"()}:dev")
}