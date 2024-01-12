package dev.nolij.zume;

import dev.nolij.zume.common.Constants;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.lexforge.LexZume;
import dev.nolij.zume.vintage.VintageZume;
import net.minecraftforge.fml.common.Mod;

@Mod(
	value = Zume.MOD_ID,
	modid = Zume.MOD_ID,
	name = Constants.MOD_NAME,
	version = Constants.MOD_VERSION,
	acceptedMinecraftVersions = Constants.VINTAGE_VERSION_RANGE,
	guiFactory = "dev.nolij.zume.vintage.VintageConfigProvider",
	dependencies = "required-after:mixinbooter@[5.0,)")
public class ForgeZumeBootstrapper {
	
	public ForgeZumeBootstrapper() {
		switch (Zume.ZUME_VARIANT) {
			case LEXFORGE -> new LexZume();
			case VINTAGE_FORGE -> new VintageZume();
		}
	}
	
}
