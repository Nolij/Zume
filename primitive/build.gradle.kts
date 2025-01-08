operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

val modCompileOnly: Configuration by configurations.creating {
	configurations.compileClasspath.get().extendsFrom(this)
}
val modRuntimeOnly: Configuration by configurations.creating {
	configurations.runtimeClasspath.get().extendsFrom(this)
}
val mod: Configuration by configurations.creating {
	configurations.compileClasspath.get().extendsFrom(this)
	configurations.runtimeClasspath.get().extendsFrom(this)
}

repositories {
	maven("https://maven.wispforest.io")
	maven("https://maven.minecraftforge.net")
}

unimined.minecraft {
	side("client")

	version("primitive_minecraft_version"())
	
	runs.config("client") {
		javaVersion = JavaVersion.VERSION_17
	}

	fabric {
		loader("babric_version"())
	}

	mappings {
		babricIntermediary()
		@Suppress("UnstableApiUsage")
		mapping("me.alphamode:nostalgia:${"primitive_mappings_version"()}:v2", "nostalgia") {
			outputs("nostalgia", true) { listOf("intermediary") }
			mapNamespace("named", "nostalgia")
			sourceNamespace("intermediary")
			renest()
		}
		devFallbackNamespace("intermediary")
	}

	mods {
		remap(modCompileOnly)
		remap(modRuntimeOnly)
		remap(mod)
	}
}

repositories {
	maven("https://maven.glass-launcher.net/snapshots")
}

dependencies {
	compileOnly(project(":stubs"))
	
	modCompileOnly(fabricApi.stationModule(moduleName = "station-keybindings-v0", version = "station_api_version"())) {
		exclude(module = "fabric-loader")
		exclude(group = "org.ow2.asm")
	}
	modRuntimeOnly("net.modificationstation:StationAPI:${"station_api_version"()}") {
		exclude(module = "cursed-fabric-loader")
		exclude(module = "fabric-loader")
		exclude(group = "org.ow2.asm")
	}

    implementation("org.slf4j:slf4j-api:${"slf4j_version"()}")
    implementation("org.apache.logging.log4j:log4j-slf4j18-impl:${"log4j_slf4j_version"()}")
}