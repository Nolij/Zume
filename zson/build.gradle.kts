operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

dependencies {
	implementation(project(":api"))
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

tasks.register<JavaExec>("testRun") {
	mainClass = "Main"
	classpath(sourceSets["test"].runtimeClasspath)
}