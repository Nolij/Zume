-dontwarn java.lang.invoke.MethodHandle

# Keep api classes and mixins
-keep class dev.nolij.zume.api.** { *; }
-keep class dev.nolij.zume.mixin.** {
	@org.spongepowered.api.mixin.** <methods>;
}
-keepattributes RuntimeVisibleAnnotations #

# optimization options
-optimizationpasses 5
-dontusemixedcaseclassnames

# obfuscation options
-printmapping build/proguard/mapping.map
-overloadaggressively
-repackageclasses 'dev.nolij.zume'
-allowaccessmodification
