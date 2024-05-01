plugins {
	id("com.github.gmazzo.buildconfig")
}

operator fun String.invoke(): String {
	return (rootProject.properties[this] as String?)!!
}

buildConfig {
	className("ZumeConstants")
	packageName("dev.nolij.zume.impl")

	useJavaOutput()

	// the below errors shown by IntelliJ can be safely ignored; Jabel works around this
	buildConfigField("MOD_ID", "mod_id"())
	buildConfigField("MOD_VERSION", Zume.version)
	buildConfigField("MOD_NAME", "mod_name"())
	buildConfigField("ARCHAIC_VERSION_RANGE", "archaic_minecraft_range"())
	buildConfigField("VINTAGE_VERSION_RANGE", "vintage_minecraft_range"())
}

dependencies {
	compileOnly("org.apache.logging.log4j:log4j-core:${"log4j_version"()}")
}

tasks.processResources {
	from("src/main/resources/assets/zume/lang/") {
		include("*.lang")
		rename { name -> name.lowercase() }
		into("assets/zume/lang/")
	}

	from("src/main/resources/assets/zume/lang/") {
		include("*.lang")
		into("assets/zume/stationapi/lang/")
	}
}