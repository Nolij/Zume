package dev.nolij.zume.vintage;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.legacyforge.LegacyForgeZume;
import dev.nolij.zume.legacyforge.ZumeKeyBind;

@Mod(modid = Zume.MOD_ID, version = Tags.MOD_VERSION, name = Zume.MOD_ID, acceptedMinecraftVersions = Tags.MC_VERSIONS)
public class VintageZume extends LegacyForgeZume {
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Zume.LOGGER.info("Loading Vintage Zume...");
		
		for (final ZumeKeyBind keyBind : ZumeKeyBind.values()) {
			ClientRegistry.registerKeyBinding(keyBind.value);
		}
		
		super.preInit();
	}
	
}
