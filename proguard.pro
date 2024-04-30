-ignorewarnings
-dontnote
-optimizationpasses 10
-optimizations !class/merging/*,!method/marking/private
-allowaccessmodification
#noinspection ShrinkerInvalidFlags
-optimizeaggressively
-overloadaggressively
-repackageclasses dev.nolij.zume
-keepattributes RuntimeVisibleAnnotations # keep annotations

-keep,allowoptimization public class dev.nolij.zume.api.** { public *; } # public APIs
-keepclassmembers class dev.nolij.zume.impl.config.ZumeConfigImpl { public <fields>; } # dont rename config fields
-keep,allowoptimization class dev.nolij.zume.ZumeMixinPlugin # dont rename mixin plugin
-keep class dev.nolij.zume.mixin.** { *; } # dont touch mixins

-keep,allowobfuscation,allowoptimization @*.*.fml.common.Mod class dev.nolij.zume.** { # Forge entrypoints
	public <init>(...);
	@*.*.fml.common.Mod$EventHandler <methods>;
	@*.*.fml.common.eventhandler.SubscribeEvent <methods>;
}
-keep,allowoptimization class dev.nolij.zume.** implements *.*.fml.client.IModGuiFactory # Legacy Forge config providers

-keepclassmembers,allowoptimization class dev.nolij.zume.** { # screens
	public void render(int,int,float);
	public void tick();
	public void init();
}
-keepclassmembers,allowoptimization class dev.nolij.zume.** extends net.minecraft.client.gui.screens.Screen { public *; }

-keep,allowoptimization class io.github.prospector.modmenu.** { *; } # ugly classloader hack

# Fabric entrypoints
-keep,allowoptimization class dev.nolij.zume.FabricZumeBootstrapper
-keep,allowoptimization class dev.nolij.zume.modern.integration.ZumeModMenuIntegration
-keep,allowoptimization class dev.nolij.zume.primitive.event.KeyBindingRegistrar { public *; }
