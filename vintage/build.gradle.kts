import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask

operator fun String.invoke(): String {
	return (rootProject.properties[this] as String?)!!
}

unimined.minecraft {
	version("vintage_minecraft_version"())

	runs {
		config("client") {
			args.addAll(arrayOf("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker"))
		}
		config("server") {
			disabled = true
		}
	}

	minecraftForge {
		loader("vintage_forge_version"())
		mixinConfig("zume-${project.name}.mixins.json")
	}

	mappings {
		searge()
		mcp("stable", "vintage_mappings_version"())
	}

	defaultRemapJar = true
}

tasks.withType<RemapJarTask> {
	mixinRemap {
		enableMixinExtra()
	}
}

repositories {
	maven("https://maven.cleanroommc.com/")
}

dependencies {
	"modImplementation"("zone.rong:mixinbooter:${"mixinbooter_version"()}")

	implementation(project(":common"))
}

tasks.processResources {
	from("common/src/main/resources")

	inputs.file("../gradle.properties")

	filteringCharset = "UTF-8"

//	filesMatching("mcmod.info") {
//		expand(rootProject.properties)
//	}
}