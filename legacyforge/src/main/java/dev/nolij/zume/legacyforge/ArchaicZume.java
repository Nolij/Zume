package dev.nolij.zume.legacyforge;

import dev.nolij.zume.common.Zume;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Zume.MOD_ID, version = Tags.MOD_VERSION, name = Zume.MOD_ID, acceptedMinecraftVersions = Tags.ARCHAIC_RANGE)
@Mod(
	modid = Zume.MOD_ID,
	name = Tags.MOD_NAME,
	version = Tags.MOD_VERSION, 
	acceptedMinecraftVersions = Tags.ARCHAIC_RANGE,
	dependencies = "required-after:unimixins@*")
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
