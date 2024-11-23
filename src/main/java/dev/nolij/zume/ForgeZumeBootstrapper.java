package dev.nolij.zume;

import dev.nolij.zume.impl.Zume;
import dev.nolij.zume.lexforge.LexZume;
import dev.nolij.zume.lexforge16.LexZume16;
import dev.nolij.zume.lexforge18.LexZume18;
import dev.nolij.zume.vintage.VintageZume;
import net.minecraftforge.fml.common.Mod;

import static dev.nolij.zume.impl.ZumeConstants.*;

@Mod(
	value = MOD_ID,
	modid = MOD_ID,
	name = MOD_NAME,
	version = MOD_VERSION,
	clientSideOnly = true,
	acceptedMinecraftVersions = VINTAGE_VERSION_RANGE,
	guiFactory = "dev.nolij.zume.vintage.VintageConfigProvider")
public class ForgeZumeBootstrapper {
	
	public ForgeZumeBootstrapper() {
		if (ZumeMixinPlugin.ZUME_VARIANT == null)
			throw new AssertionError("""
				Mixins did not load! Zume requires Mixins in order to work properly.
				Please install one of the following mixin loaders:
				14.4 - 16.0: MixinBootstrap
				8.9 - 12.2: MixinBooter >= 5.0
				7.10 - 12.2: UniMixins >= 0.1.15""");
		
		switch (ZumeMixinPlugin.ZUME_VARIANT) {
			case ZumeMixinPlugin.LEXFORGE -> new LexZume();
			case ZumeMixinPlugin.LEXFORGE18 -> new LexZume18();
			case ZumeMixinPlugin.LEXFORGE16 -> new LexZume16();
			case ZumeMixinPlugin.VINTAGE_FORGE -> new VintageZume();
		}
		
		Zume.postInit();
	}
	
}
