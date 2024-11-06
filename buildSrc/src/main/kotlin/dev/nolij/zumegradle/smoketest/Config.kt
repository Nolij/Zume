package dev.nolij.zumegradle.smoketest

data class Config(
	val modLoader: String,
	val mcVersion: String,
	val loaderVersion: String? = null,
	val jvmVersion: Int? = null,
	val extraArgs: List<String>? = null,
	val dependencies: List<String>? = null,
) {
	val versionString: String get() =
		if (loaderVersion != null)
			"${modLoader}:${mcVersion}:${loaderVersion}"
		else
			"${modLoader}:${mcVersion}"

	override fun toString(): String {
		val result = StringBuilder()

		result.appendLine("modLoader=${modLoader}")
		result.appendLine("mcVersion=${mcVersion}")
		result.appendLine("loaderVersion=${loaderVersion}")
		result.appendLine("jvmVersion=${jvmVersion}")
		result.appendLine("extraArgs=[${extraArgs?.joinToString(", ") ?: ""}]")
		result.appendLine("mods=[${dependencies?.joinToString(", ") { it.split(":")[1] } ?: ""}]")

		return result.toString()
	}
}