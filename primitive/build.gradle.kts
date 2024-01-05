operator fun String.invoke(): String {
	return (rootProject.properties[this] as String?)!!
}

unimined.minecraft {
	side("client")

	version("primitive_minecraft_version"())
	
	runs {
		config("client") {
			javaVersion = JavaVersion.VERSION_17
		}
	}

	babric {
		loader("primitive_fabric_version"())
	}

	mappings {
		babricIntermediary()
		biny("primitive_mappings_version"())
		devFallbackNamespace("intermediary")
	}

	defaultRemapJar = true
}

repositories {
	maven("https://maven.glass-launcher.net/snapshots")
}

dependencies {
    "modImplementation"(fabricApi.stationModule(moduleName = "station-keybindings-v0", version = "station_api_version"()))
	
	val mixinExtras = "io.github.llamalad7:mixinextras-fabric:${"mixinextras_version"()}"
//	annotationProcessor(mixinExtras)
	implementation(mixinExtras)

    implementation("org.slf4j:slf4j-api:1.8.0-beta4")
    implementation("org.apache.logging.log4j:log4j-slf4j18-impl:2.16.0")

    implementation(project(":common"))
}

tasks.processResources {
	from("common/src/main/resources")

	filteringCharset = "UTF-8"

	filesMatching("fabric.mod.json") {
		expand(rootProject.properties)
	}
}