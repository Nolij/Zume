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
	buildConfigField("MOD_VERSION", "mod_version"())
	buildConfigField("MOD_NAME", "mod_name"())
	buildConfigField("ARCHAIC_VERSION_RANGE", "archaic_minecraft_range"())
	buildConfigField("VINTAGE_VERSION_RANGE", "vintage_minecraft_range"())
}

repositories {
	maven("https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
    // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core
	compileOnly("org.apache.logging.log4j:log4j-core:2.22.0")
	
	compileOnly("org.ow2.asm:asm-tree:9.6")
	compileOnly("org.spongepowered:mixin:0.8.5")
}