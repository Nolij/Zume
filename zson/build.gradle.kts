operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

tasks.register<JavaExec>("testRun") {
	mainClass = "Main"
	classpath(sourceSets["test"].runtimeClasspath)
}