import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask

operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

unimined.minecraft {
	side("client")

	version("primitive_minecraft_version"())
	
	runs.config("client") {
		javaVersion = JavaVersion.VERSION_17
	}

	babric {
		loader("babric_version"())
	}

	mappings {
		babricIntermediary()
		biny("primitive_mappings_version"())
		devFallbackNamespace("intermediary")
	}
}

tasks.withType<RemapJarTask> {
	mixinRemap {
		enableMixinExtra()
		disableRefmap()
	}
}

repositories {
	maven("https://maven.glass-launcher.net/snapshots")
}

dependencies {
	compileOnly(project(":stubs"))
	
	"modImplementation"(fabricApi.stationModule(moduleName = "station-keybindings-v0", version = "station_api_version"())) {
		exclude(module = "fabric-loader")
		exclude(group = "org.ow2.asm")
	}
	"modImplementation"("net.modificationstation:StationAPI:2.0-PRE2") {
		exclude(module = "cursed-fabric-loader")
		exclude(module = "fabric-loader")
		exclude(group = "org.ow2.asm")
	}
	
	implementation("io.github.llamalad7:mixinextras-fabric:${"mixinextras_version"()}")

    implementation("org.slf4j:slf4j-api:${"slf4j_version"()}")
    implementation("org.apache.logging.log4j:log4j-slf4j18-impl:${"log4j_slf4j_version"()}")
}