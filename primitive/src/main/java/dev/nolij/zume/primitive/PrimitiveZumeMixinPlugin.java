package dev.nolij.zume.primitive;

import dev.nolij.zume.common.Zume;
import dev.nolij.zume.common.ZumeMixinPlugin;
import dev.nolij.zume.common.ZumeVariant;

public class PrimitiveZumeMixinPlugin extends ZumeMixinPlugin {
	
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return Zume.ZUME_VARIANT == ZumeVariant.PRIMITIVE;
	}
	
}
