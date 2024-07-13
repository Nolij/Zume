import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask

operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

unimined.minecraft {
	version("vintage_minecraft_version"())

	runs.config("client") {
		jvmArguments.addAll("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
	}

	minecraftForge {
		loader("vintage_forge_version"())
		mixinConfig("zume-${project.name}.mixins.json")
	}

	mappings {
		searge()
		mcp("stable", "vintage_mappings_version"())
	}
}

repositories {
	maven("https://maven.cleanroommc.com/")
}

dependencies {
	compileOnly(project(":stubs"))
	
	"modImplementation"("zone.rong:mixinbooter:${"mixinbooter_version"()}")
}