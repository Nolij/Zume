package dev.nolij.zume.modern;

import dev.nolij.zume.common.Zume;
import dev.nolij.zume.common.ZumeMixinPlugin;
import dev.nolij.zume.common.ZumeVariant;

public class ModernZumeMixinPlugin extends ZumeMixinPlugin {
	
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return Zume.ZUME_VARIANT == ZumeVariant.MODERN;
	}
	
}
