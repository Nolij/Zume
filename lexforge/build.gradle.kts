import xyz.wagyourtail.unimined.internal.minecraft.patch.forge.ForgeLikeMinecraftTransformer

operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

val modRuntimeOnly: Configuration by configurations.creating {
	configurations.runtimeClasspath.get().extendsFrom(this)
}

unimined.minecraft {
	combineWith(project(":integration:embeddium").sourceSets.main.get())

	version("lexforge_minecraft_version"())

	minecraftForge {
		loader("lexforge_version"())
		mixinConfig("zume-lexforge.mixins.json")
	}

	mappings {
		mojmap()
		parchment(mcVersion = "lexforge_minecraft_version"(), version = "lexforge_parchment_version"())
	}

	mods {
		remap(modRuntimeOnly) {
			mixinRemap {
				off()
			}
		}
	}

	runs.config("client") {
		jvmArgs.addAll(
			listOf(
				"-Dmixin.env.remapRefMap=true",
				"-Dmixin.env.refMapRemappingFile=${(mcPatcher as ForgeLikeMinecraftTransformer).srgToMCPAsSRG}"
			)
		)
	}
}

repositories {
	maven("https://maven.blamejared.com")
}

dependencies {
	compileOnly(project(":stubs"))

	// mixins fail to apply due to Unimined not liking Embeddium's empty mixin list; test in prod
//	modRuntimeOnly("org.embeddedt:embeddium-1.20.1:${"embeddium_lexforge_version"()}")
}