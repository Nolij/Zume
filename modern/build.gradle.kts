import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask

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

unimined.minecraft {
	combineWith(project(":integration:embeddium").sourceSets.main.get())
	
	version("modern_minecraft_version"())

	fabric {
		loader("fabric_version"())
	}

	mappings {
		intermediary()
		mojmap()
		devFallbackNamespace("intermediary")
	}
	
	mods {
		remap(modCompileOnly)
		remap(modRuntimeOnly)
		remap(mod)
	}
}

repositories {
	maven("https://maven.terraformersmc.com/releases/")
	maven("https://maven.blamejared.com")
}

dependencies {
	compileOnly(project(":stubs"))
	
	modCompileOnly(fabricApi.fabricModule("fabric-key-binding-api-v1", "modern_fabric_api_version"()))
	modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:${"modern_fabric_api_version"()}")

	mod("com.terraformersmc:modmenu:7.+")

	modRuntimeOnly("org.embeddedt:embeddium-fabric-1.20.1:${"embeddium_fabric_version"()}")
}