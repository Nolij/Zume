import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask

operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

unimined.minecraft {
	version("legacy_minecraft_version"())

	fabric {
		loader("fabric_version"())
	}

	mappings {
		legacyIntermediary()
		legacyYarn("legacy_mappings_version"())
	}
}

tasks.withType<RemapJarTask> {
	mixinRemap {
		enableMixinExtra()
		disableRefmap()
	}
}

dependencies {
	"modImplementation"(
		fabricApi.legacyFabricModule(
			"legacy-fabric-keybindings-api-v1-common",
			"legacy_fabric_api_version"()
		)
	)
}