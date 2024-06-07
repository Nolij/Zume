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

-keep,allowoptimization public class dev.nolij.zume.api.** { public *; } # public APIs
-keepclassmembers class dev.nolij.zume.impl.config.ZumeConfigImpl { public <fields>; } # dont rename config fields
-keepclassmembers,allowoptimization class dev.nolij.zume.ZumeMixinPlugin {
    public *;
}
-keep @org.spongepowered.asm.mixin.Mixin class * { *; } # dont touch mixins

# Forge entrypoints
-keep,allowobfuscation @*.*.fml.common.Mod class dev.nolij.zume.** {
	public <init>(...);
}

# Platform implementations
-keep,allowobfuscation class dev.nolij.zume.** implements dev.nolij.zume.api.platform.v*.IZumeImplementation {
	# Forge Event Subscribers
	@*.*.fml.common.Mod$EventHandler <methods>;
	@*.*.fml.common.eventhandler.SubscribeEvent <methods>;
}

-adaptclassstrings
-adaptresourcefilecontents fabric.mod.json

# screens
-keepclassmembers class dev.nolij.zume.** extends net.minecraft.class_437,
												  net.minecraft.client.gui.screens.Screen,
												  net.minecraft.client.gui.screen.Screen {
	public *;
}

# Legacy Forge config providers
-keep,allowoptimization,allowobfuscation class dev.nolij.zume.** implements *.*.fml.client.IModGuiFactory
-keepclassmembers,allowoptimization class dev.nolij.zume.** extends *.*.fml.client.config.GuiConfig {
	public <methods>;
}

# Fabric entrypoints
-keep,allowoptimization,allowobfuscation class dev.nolij.zume.FabricZumeBootstrapper
-keep,allowoptimization,allowobfuscation class dev.nolij.zume.modern.integration.modmenu.ZumeModMenuIntegration
-keep,allowoptimization,allowobfuscation class dev.nolij.zume.primitive.event.KeyBindingRegistrar { public *; }

-keep @dev.nolij.zumegradle.proguard.ProGuardKeep class * { *; }
-keepclassmembers class * { @dev.nolij.zumegradle.proguard.ProGuardKeep *; }