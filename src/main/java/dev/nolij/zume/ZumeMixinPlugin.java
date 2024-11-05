package dev.nolij.zume;

import dev.nolij.libnolij.refraction.Refraction;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class ZumeMixinPlugin implements IMixinConfigPlugin {
	
	static final String MODERN = "modern";
	static final String PRIMITIVE = "primitive";
	static final String LEGACY = "legacy";
	static final String ARCHAIC_FORGE = "archaic";
	static final String VINTAGE_FORGE = "vintage";
	static final String LEXFORGE = "lexforge";
	static final String LEXFORGE18 = "lexforge18";
	static final String LEXFORGE16 = "lexforge16";
	
	private static final ClassLoader CLASS_LOADER = ZumeMixinPlugin.class.getClassLoader();
	
	static final String ZUME_VARIANT;
	private static final String implementationMixinPackage;
	
	static {
		String forgeVersion = null;
		
		try {
			//noinspection DataFlowIssue
			forgeVersion = (String) Refraction.safe().getMethodOrNull(
				Refraction.safe().getClassOrNull("net.minecraftforge.versions.forge.ForgeVersion"),
				"getVersion"
			).invokeExact();
		} catch (Throwable ignored) { }
		
		if (forgeVersion != null) {
			final int major = Integer.parseInt(forgeVersion.substring(0, forgeVersion.indexOf('.')));
			if (major > 40)
				ZUME_VARIANT = LEXFORGE;
			else if (major > 36)
				ZUME_VARIANT = LEXFORGE18;
			else if (major > 25)
				ZUME_VARIANT = LEXFORGE16;
			else
				ZUME_VARIANT = null;
		} else {
			if (CLASS_LOADER.getResource("net/fabricmc/fabric/api/client/keybinding/v1/KeyBindingHelper.class") != null)
				ZUME_VARIANT = MODERN;
			else if (CLASS_LOADER.getResource("net/legacyfabric/fabric/api/client/keybinding/v1/KeyBindingHelper.class") != null)
				ZUME_VARIANT = LEGACY;
			else if (CLASS_LOADER.getResource("net/modificationstation/stationapi/api/client/event/option/KeyBindingRegisterEvent.class") != null)
				ZUME_VARIANT = PRIMITIVE;
			else if (
				CLASS_LOADER.getResource("cpw/mods/fml/client/registry/KeyBindingRegistry.class") == null &&
				CLASS_LOADER.getResource("cpw/mods/fml/client/registry/ClientRegistry.class") != null)
				ZUME_VARIANT = ARCHAIC_FORGE;
			else if (CLASS_LOADER.getResource("net/minecraftforge/oredict/OreDictionary.class") != null)
				ZUME_VARIANT = VINTAGE_FORGE;
			else
				ZUME_VARIANT = null;
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
