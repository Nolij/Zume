package dev.nolij.zume.common;

public class LegacyForgeZumeMixinPlugin extends ZumeMixinPlugin {
	
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		switch (Zume.ZUME_VARIANT) {
			case ARCHAIC_FORGE -> {
				return mixinClassName.startsWith("dev.nolij.zume.archaic");
			}
			case VINTAGE_FORGE -> {
				return mixinClassName.startsWith("dev.nolij.zume.vintage");
			}
		}
		
		return false;
	}
	
}
