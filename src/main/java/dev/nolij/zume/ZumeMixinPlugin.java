package dev.nolij.zume;

import dev.nolij.zume.api.util.v1.MethodHandleHelper;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Set;

public final class ZumeMixinPlugin implements IMixinConfigPlugin {
	
	private static final ClassLoader CLASS_LOADER = ZumeMixinPlugin.class.getClassLoader();
	
	static final String ZUME_VARIANT;
	private static final String implementationMixinPackage;
	
	static {
		if (MethodHandleHelper.PUBLIC.getClassOrNull("dev.su5ed.sinytra.connector.service.ConnectorLoaderService") == null &&
			CLASS_LOADER.getResource("net/fabricmc/fabric/api/client/keybinding/v1/KeyBindingHelper.class") != null)
			ZUME_VARIANT = ZumeVariant.MODERN;
		else if (CLASS_LOADER.getResource("net/legacyfabric/fabric/api/client/keybinding/v1/KeyBindingHelper.class") != null)
			ZUME_VARIANT = ZumeVariant.LEGACY;
		else if (CLASS_LOADER.getResource("net/modificationstation/stationapi/api/client/event/option/KeyBindingRegisterEvent.class") != null)
			ZUME_VARIANT = ZumeVariant.PRIMITIVE;
		else if (
			CLASS_LOADER.getResource("cpw/mods/fml/client/registry/KeyBindingRegistry.class") == null &&
				CLASS_LOADER.getResource("cpw/mods/fml/client/registry/ClientRegistry.class") != null)
			ZUME_VARIANT = ZumeVariant.ARCHAIC_FORGE;
		else if (CLASS_LOADER.getResource("net/minecraftforge/oredict/OreDictionary.class") != null)
			ZUME_VARIANT = ZumeVariant.VINTAGE_FORGE;
		else {
			String forgeVersion = null;
			
			try {
				//noinspection DataFlowIssue
				forgeVersion = (String) MethodHandleHelper.PUBLIC.getMethodOrNull(
					MethodHandleHelper.PUBLIC.getClassOrNull("net.minecraftforge.versions.forge.ForgeVersion"),
					"getVersion",
					MethodType.methodType(String.class)
				).invokeExact();
			} catch (Throwable ignored) { }
			
			if (forgeVersion != null) {
				final int major = Integer.parseInt(forgeVersion.substring(0, forgeVersion.indexOf('.')));
				if (major > 40)
					ZUME_VARIANT = ZumeVariant.LEXFORGE;
				else if (major > 36)
					ZUME_VARIANT = ZumeVariant.LEXFORGE18;
				else if (major > 25)
					ZUME_VARIANT = ZumeVariant.LEXFORGE16;
				else
					ZUME_VARIANT = null;
			} else {
				ZUME_VARIANT = null;
			}
		}
		
		if (ZUME_VARIANT != null)
			implementationMixinPackage = "dev.nolij.zume.mixin." + ZUME_VARIANT + ".";
		else
			implementationMixinPackage = null;
	}
	
	@Override
	public void onLoad(String mixinPackage) {}
	
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (implementationMixinPackage == null)
			return false;
		
		return mixinClassName.startsWith(implementationMixinPackage);
	}
	
	@Override
	public String getRefMapperConfig() {
		return null;
	}
	
	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
	
	@Override
	public List<String> getMixins() {
		return null;
	}
	
	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
	
	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
	
}
