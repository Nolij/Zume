import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask

operator fun String.invoke(): String {
	return (rootProject.properties[this] as String?)!!
}

unimined.minecraft {
	combineWith(project(":common").sourceSets.main.get())

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

//tasks.withType<RemapJarTask> {
//	mixinRemap {
//		enableMixinExtra()
//	}
//}

dependencies {
	"minecraftLibraries"("blue.endless:jankson:${"jankson_version"()}")

//	val mixinExtrasCommon = "io.github.llamalad7:mixinextras-common:${"mixinextras_version"()}"
//	compileOnly(mixinExtrasCommon)
//	annotationProcessor(mixinExtrasCommon)
//	val mixinExtrasForge = "io.github.llamalad7:mixinextras-forge:${"mixinextras_version"()}"
//	"minecraftLibraries"(mixinExtrasForge)
}

tasks.processResources {
	from("common/src/main/resources")

	inputs.file("../gradle.properties")

	filteringCharset = "UTF-8"

	filesMatching("META-INF/mods.toml") {
		expand(rootProject.properties)
	}
}