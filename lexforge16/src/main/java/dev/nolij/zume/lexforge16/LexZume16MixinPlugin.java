package dev.nolij.zume.lexforge16;

import dev.nolij.zume.common.Zume;
import dev.nolij.zume.common.ZumeMixinPlugin;
import dev.nolij.zume.common.ZumeVariant;

public class LexZume16MixinPlugin extends ZumeMixinPlugin {
	
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return Zume.ZUME_VARIANT == ZumeVariant.LEXFORGE16;
	}
	
}
