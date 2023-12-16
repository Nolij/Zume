package dev.nolij.zume.archaic;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import dev.nolij.zume.legacyforge.LegacyForgeZume;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.legacyforge.ZumeKeyBind;

@Mod(modid = Zume.MOD_ID, version = Tags.MOD_VERSION, name = Zume.MOD_ID, acceptedMinecraftVersions = Tags.MC_VERSIONS)
public class ArchaicZume extends LegacyForgeZume {
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Zume.LOGGER.info("Loading Archaic Zume...");
		
		for (final ZumeKeyBind keyBind : ZumeKeyBind.values()) {
			ClientRegistry.registerKeyBinding(keyBind.value);
		}
		
		super.preInit();
	}
	
}