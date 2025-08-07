package dev.nolij.zumegradle.smoketest

import groovy.json.JsonSlurper
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.util.concurrent.TimeUnit

@Suppress("UNCHECKED_CAST")
object MojangMetaApi {
	private const val MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
	private val JSON = JsonSlurper()
	
	fun getJavaVersion(mcVersion: String): Int {
		if(mcVersion == "snapshot") {
			return getJavaVersion(getLatestSnapshot())
		} else if(mcVersion == "release") {
			return getJavaVersion(getLatestRelease())
		}
		val manifest = request(MANIFEST) as Map<String, Any>
		val versions = manifest["versions"] as List<Map<String, Any>>
		val version = versions.firstOrNull { it["id"] == mcVersion } 
			?: error("invalid minecraft version: $mcVersion")
		
		val versionManifest = request(version["url"] as String) as Map<String, Any>
		val javaVersion = versionManifest["javaVersion"] as Map<String, Any>
		return javaVersion["majorVersion"] as Int
	}
	
	fun getLatestRelease(): String {
		val manifest = request(MANIFEST) as Map<String, Any>
		val latest = manifest["latest"] as Map<String, String>
		return latest["release"]!!
	}
	
	fun getLatestSnapshot(): String {
		val manifest = request(MANIFEST) as Map<String, Any>
		val latest = manifest["latest"] as Map<String, String>
		return latest["snapshot"]!!
	}

	private val cache = mutableMapOf<String, Any>()
	private fun request(url: String): Any {
		if(url in cache)
			return cache[url]!!
		
		val data = JSON.parseText(requestImpl(url).bufferedReader().readText())
		cache[url] = data
		return data
	}
	
	private fun requestImpl(url: String): InputStream {
		val connection = URI(url).toURL().openConnection() as HttpURLConnection
		connection.requestMethod = "GET"
		@Suppress("DEPRECATION")
		connection.setRequestProperty("User-Agent", "ZumeGradle/${Zume.version} (https://github.com/Nolij/Zume)")
		connection.connectTimeout = TimeUnit.SECONDS.toMillis(10).toInt()
		connection.readTimeout = TimeUnit.SECONDS.toMillis(5).toInt()
		connection.connect()
		
		if (connection.responseCode != 200)
			error("Failed to request $url: ${connection.responseCode}")
		
		return connection.inputStream
	}
}