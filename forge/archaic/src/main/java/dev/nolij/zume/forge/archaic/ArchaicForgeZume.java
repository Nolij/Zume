package dev.nolij.zume.forge.archaic;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import dev.nolij.zume.common.Zume;

@Mod(modid = Zume.MOD_ID, version = "0.1.0", name = Zume.MOD_ID, acceptedMinecraftVersions = "[1.7.10]")
public class ArchaicForgeZume {
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Zume.LOGGER.info("Hello Archaic Forge world.");
    }
}
