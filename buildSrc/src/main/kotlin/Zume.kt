@Suppress("MemberVisibilityCanBePrivate")
object Zume {
	private var _version: String? = null
	@get:Deprecated("Use TauGradle Versioning", level = DeprecationLevel.WARNING)
	var version: String = ""
		get() = _version!!
		set(value) {
			field = value
			if (value.isNotEmpty())
				_version = value
		}
	
	var auditAndExitEnabled = false
}