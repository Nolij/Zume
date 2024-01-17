import java.net.URI

operator fun String.invoke(): String {
	return (rootProject.properties[this] as String?)!!
}

unimined.minecraft {
	version("rift_minecraft_version"())

	runs {
		config("server") {
			disabled = true
		}
	}

	@Suppress("UnstableApiUsage")
	minecraftData.metadataURL = URI.create("https://skyrising.github.io/mc-versions/manifest/f/f/8444b7446a793191e0c496bba07ac41ff17031/1.13.2.json")

	rift {}

	mappings {
		searge()
		mcp("snapshot", "rift_mappings_version"())
	}

	@Suppress("UnstableApiUsage")
	minecraftRemapper.config {
		ignoreConflicts(true)
	}

	defaultRemapJar = true
}

dependencies {
	implementation(project(":common"))
}

tasks.processResources {
	from("common/src/main/resources")

	filteringCharset = "UTF-8"

	filesMatching("riftmod.json") {
		expand(rootProject.properties)
	}
}