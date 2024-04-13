package dev.nolij.zume;

import dev.nolij.zume.common.Constants;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.common.ZumeVariant;
import dev.nolij.zume.lexforge.LexZume;
import dev.nolij.zume.lexforge18.LexZume18;
import dev.nolij.zume.lexforge16.LexZume16;
import dev.nolij.zume.vintage.VintageZume;
import net.minecraftforge.fml.common.Mod;

@Mod(
	value = Zume.MOD_ID,
	modid = Zume.MOD_ID,
	name = Constants.MOD_NAME,
	version = Constants.MOD_VERSION,
	acceptedMinecraftVersions = Constants.VINTAGE_VERSION_RANGE,
	guiFactory = "dev.nolij.zume.vintage.VintageConfigProvider")
public class ForgeZumeBootstrapper {
	
	public ForgeZumeBootstrapper() {
		if (Zume.ZUME_VARIANT == null)
			throw new AssertionError("""
				Mixins did not load! Zume requires Mixins in order to work properly.
				Please install one of the following mixin loaders:
				14.4 - 16.0: MixinBootstrap
				8.9 - 12.2: MixinBooter >= 5.0
				7.10 - 12.2: UniMixins >= 0.1.15""");
		
		switch (Zume.ZUME_VARIANT) {
			case ZumeVariant.LEXFORGE -> new LexZume();
			case ZumeVariant.LEXFORGE18 -> new LexZume18();
			case ZumeVariant.LEXFORGE16 -> new LexZume16();
			case ZumeVariant.VINTAGE_FORGE -> new VintageZume();
		}
	}
	
}
