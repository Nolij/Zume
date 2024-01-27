import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask

operator fun String.invoke(): String {
	return (rootProject.properties[this] as String?)!!
}

unimined.minecraft {
	version("archaic_minecraft_version"())
	
	runs {
		config("server") {
			disabled = true
		}
	}
	
	minecraftForge {
		loader("archaic_forge_version"())
		mixinConfig("zume-${project.name}.mixins.json")
	}
	
	mappings {
		searge()
		mcp("stable", "archaic_mappings_version"())
	}
	
	defaultRemapJar = true
}

tasks.withType<RemapJarTask> {
	mixinRemap {
		enableMixinExtra()
	}
}

dependencies {
	"modImplementation"("com.github.LegacyModdingMC.UniMixins:unimixins-all-1.7.10:${"unimixins_version"()}:dev")
	
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