-ignorewarnings
-dontnote
-optimizationpasses 10
-optimizations !class/merging/*,!method/marking/private,!*/specialization/*
-allowaccessmodification
#noinspection ShrinkerInvalidFlags
-optimizeaggressively
-overloadaggressively
-repackageclasses zume
-keepattributes Runtime*Annotations # keep annotations

-keep,allowoptimization public class dev.nolij.zume.api.** { public *; } # public APIs
-keepclassmembers class dev.nolij.zume.impl.config.ZumeConfigImpl { public <fields>; } # dont rename config fields
-keep,allowoptimization class dev.nolij.zume.ZumeMixinPlugin # dont rename mixin plugin
-keep @org.spongepowered.asm.mixin.Mixin class dev.nolij.zume.** { *; } # dont touch mixins

# Forge entrypoints
-keep,allowobfuscation @*.*.fml.common.Mod class dev.nolij.zume.** {
	public <init>(...);
}

# Platform implementations
-keep,allowobfuscation class dev.nolij.zume.** implements dev.nolij.zume.api.platform.v0.IZumeImplementation {
	# Forge Event Subscribers
	@*.*.fml.common.Mod$EventHandler <methods>;
	@*.*.fml.common.eventhandler.SubscribeEvent <methods>;
}

# screens
-keepclassmembers class dev.nolij.zume.** {
	void render(int,int,float);
	void tick();
	void init();
	void method_25423(); # intermediary name for init()
}
-keepclassmembers,allowoptimization class dev.nolij.zume.** extends net.minecraft.client.gui.screens.Screen {
	public *; 
}

# Legacy Forge config providers
-keep,allowoptimization class dev.nolij.zume.** implements *.*.fml.client.IModGuiFactory
-keep,allowoptimization class dev.nolij.zume.** extends *.*.fml.client.config.GuiConfig { *; }

-keep,allowoptimization class io.github.prospector.modmenu.** { *; } # ugly classloader hack

# Fabric entrypoints
-keep,allowoptimization class dev.nolij.zume.FabricZumeBootstrapper
-keep,allowoptimization class dev.nolij.zume.modern.integration.ZumeModMenuIntegration
-keep,allowoptimization class dev.nolij.zume.primitive.event.KeyBindingRegistrar { public *; }
