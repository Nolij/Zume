operator fun String.invoke(): String {
	return (rootProject.properties[this] as String?)!!
}

unimined.minecraft {
	version("modern_minecraft_version"())
	
	runs {
		config("server") {
			disabled = true
		}
	}

	fabric {
		loader("fabric_version"())
	}

	mappings {
		intermediary()
		yarn("modern_mappings_version"())
		devFallbackNamespace("intermediary")
	}

	defaultRemapJar = true
}

repositories {
	maven("https://maven.terraformersmc.com/releases/")
}

dependencies {
	compileOnly(project(":stubs"))
	
	"modImplementation"(fabricApi.fabricModule("fabric-key-binding-api-v1", "modern_fabric_api_version"()))
	
	"modImplementation"("com.terraformersmc:modmenu:7.+")
}