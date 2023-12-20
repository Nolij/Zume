package dev.nolij.zume.common;

public class LegacyForgeZumeMixinPlugin extends ZumeMixinPlugin {
	
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return (
			Zume.ZUME_VARIANT == ZumeVariant.ARCHAIC_FORGE &&
			mixinClassName.startsWith("dev.nolij.zume.legacyforge.mixin.archaic")) || (
			Zume.ZUME_VARIANT == ZumeVariant.VINTAGE_FORGE &&
			mixinClassName.startsWith("dev.nolij.zume.legacyforge.mixin.vintage"));
	}
	
}
