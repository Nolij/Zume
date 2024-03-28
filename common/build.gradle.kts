plugins {
	id("com.github.gmazzo.buildconfig")
}

operator fun String.invoke(): String {
	return (rootProject.properties[this] as String?)!!
}

buildConfig {
	className("Constants")
	packageName("dev.nolij.zume.common")

	useJavaOutput()

	// the below errors shown by IntelliJ can be safely ignored; Jabel works around this
	buildConfigField("MOD_VERSION", Zume.version)
	buildConfigField("MOD_NAME", "mod_name"())
	buildConfigField("ARCHAIC_VERSION_RANGE", "archaic_minecraft_range"())
	buildConfigField("VINTAGE_VERSION_RANGE", "vintage_minecraft_range"())
}

repositories {
	maven("https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
	compileOnly("org.apache.logging.log4j:log4j-core:${"log4j_version"()}")
	
	compileOnly("org.ow2.asm:asm-tree:${"asm_version"()}")
	compileOnly("org.spongepowered:mixin:${"mixin_version"()}")
}