package dev.nolij.zume.lexforge;

import dev.nolij.zume.common.Zume;
import dev.nolij.zume.common.ZumeMixinPlugin;
import dev.nolij.zume.common.ZumeVariant;

public class LexZumeMixinPlugin extends ZumeMixinPlugin {
	
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return Zume.ZUME_VARIANT == ZumeVariant.LEXFORGE;
	}
	
}
