-ignorewarnings
-dontnote
-optimizationpasses 10
-optimizations !class/merging/*,!method/marking/private,!method/marking/static,!*/specialization/*,!method/removal/parameter
-allowaccessmodification
#noinspection ShrinkerInvalidFlags
-optimizeaggressively
-overloadaggressively
-repackageclasses zume
-keepattributes Runtime*Annotations # keep annotations
-adaptclassstrings
-adaptresourcefilecontents fabric.mod.json

-keep @dev.nolij.zumegradle.proguard.ProGuardKeep class * { *; }
-keepclassmembers class * { @dev.nolij.zumegradle.proguard.ProGuardKeep *; }

-keep,allowoptimization,allowobfuscation @dev.nolij.zumegradle.proguard.ProGuardKeep$WithObfuscation class * { *; }
-keepclassmembers,allowoptimization,allowobfuscation class * { @dev.nolij.zumegradle.proguard.ProGuardKeep$WithObfuscation *; }
