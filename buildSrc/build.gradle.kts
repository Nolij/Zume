plugins {
	`kotlin-dsl`
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.ow2.asm:asm-tree:9.7")
	implementation("com.google.code.gson:gson:2.10.1")
}