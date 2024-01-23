package dev.nolij.zume.common;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ZumeMixinPlugin implements IMixinConfigPlugin {
	
	private String implementationMixinPackage = null;
	
	@Override
	public void onLoad(String mixinPackage) {
		Zume.calculateZumeVariant();
        if (Zume.ZUME_VARIANT != null)
            implementationMixinPackage = "dev.nolij.zume." + Zume.ZUME_VARIANT.name + ".mixin.";
	}
	
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
