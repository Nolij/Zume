operator fun String.invoke(): String {
	return (rootProject.properties[this] as String?)!!
}

unimined.minecraft {
	version("legacy_minecraft_version"())
	
	runs {
		config("server") {
			disabled = true
		}
	}

	fabric {
		loader("fabric_version"())
	}

	mappings {
		legacyIntermediary()
		legacyYarn("legacy_mappings_version"())
	}

	defaultRemapJar = true
}

dependencies {
    "modImplementation"(fabricApi.legacyFabricModule("legacy-fabric-keybindings-api-v1-common", "legacy_fabric_api_version"()))

    implementation(project(":common"))
}

tasks.processResources {
	from("common/src/main/resources")

	inputs.file("../gradle.properties")

	filteringCharset = "UTF-8"

	filesMatching("fabric.mod.json") {
		expand(rootProject.properties)
	}
}