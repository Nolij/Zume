package dev.nolij.zumegradle

import com.github.jengelman.gradle.plugins.shadow.transformers.Transformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.file.FileTreeElement
import org.gradle.api.tasks.Input
import org.apache.tools.zip.ZipOutputStream
import org.apache.tools.zip.ZipEntry

class MixinConfigMergingTransformer : Transformer {
	
	private val JSON = JsonSlurper()

	@Input
	lateinit var modId: String
	@Input
	lateinit var packageName: String
	@Input
	lateinit var mixinPlugin: String

	override fun getName(): String {
		return "MixinConfigMergingTransformer"
	}

	override fun canTransformResource(element: FileTreeElement?): Boolean {
		return element != null && (element.name.endsWith(".mixins.json") || element.name.endsWith("-refmap.json"))
	}

	private var transformed = false

	private var mixins = ArrayList<String>()
	private var refMaps = HashMap<String, Map<String, String>>()

	override fun transform(context: TransformerContext?) {
		if (context == null)
			return

		this.transformed = true

		val parsed = JSON.parse(context.`is`) as Map<*, *>
		if (parsed.contains("client")) {
			@Suppress("UNCHECKED_CAST")
			mixins.addAll(parsed["client"] as List<String>)
		} else {
			@Suppress("UNCHECKED_CAST")
			refMaps.putAll(parsed["mappings"] as Map<String, Map<String, String>>)
		}
	}

	override fun hasTransformedResource(): Boolean {
		return transformed
	}

	override fun modifyOutputStream(os: ZipOutputStream?, preserveFileTimestamps: Boolean) {
		val mixinConfigEntry = ZipEntry("${modId}.mixins.json")
		os!!.putNextEntry(mixinConfigEntry)
		os.write(
			JsonOutput.prettyPrint(
				JsonOutput.toJson(mapOf(
					"required" to true,
					"minVersion" to "0.8",
					"package" to packageName,
					"plugin" to mixinPlugin,
					"compatibilityLevel" to "JAVA_8",
					"client" to mixins,
					"injectors" to mapOf(
						"defaultRequire" to 1,
					),
					"refmap" to "${modId}-refmap.json",
				))).toByteArray())

		val refMapEntry = ZipEntry("${modId}-refmap.json")
		os.putNextEntry(refMapEntry)
		os.write(
			JsonOutput.prettyPrint(
				JsonOutput.toJson(mapOf(
					"mappings" to refMaps,
				))).toByteArray())

		transformed = false
		mixins.clear()
		refMaps.clear()
	}
	
}