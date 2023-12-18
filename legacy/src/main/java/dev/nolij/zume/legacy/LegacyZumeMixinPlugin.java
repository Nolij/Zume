package dev.nolij.zume.legacy;

import dev.nolij.zume.common.Zume;
import dev.nolij.zume.common.ZumeMixinPlugin;
import dev.nolij.zume.common.ZumeVariant;

public class LegacyZumeMixinPlugin extends ZumeMixinPlugin {
	
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return Zume.ZUME_VARIANT == ZumeVariant.LEGACY;
	}
	
}
