operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

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
}