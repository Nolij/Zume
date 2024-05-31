plugins {
	id("xyz.wagyourtail.unimined")
}

operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

unimined.minecraft {
	combineWith(project(":api").sourceSets.main.get())

	version("modern_minecraft_version"())

	runs.off = true

	fabric {
		loader("fabric_version"())
	}

	mappings {
		mojmap()
	}

	defaultRemapJar = false
}

repositories {
	maven("https://maven.blamejared.com")
}

dependencies {
	"modImplementation"("org.embeddedt:embeddium-fabric-1.20.1:${"embeddium_fabric_version"()}")
}