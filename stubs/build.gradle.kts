operator fun String.invoke(): String {
	return (rootProject.properties[this] as String?)!!
}