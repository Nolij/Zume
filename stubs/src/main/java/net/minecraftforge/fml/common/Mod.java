package net.minecraftforge.fml.common;

public @interface Mod {
	
	// Pre/Modern Forge
	String value();
	
	// Vintage Forge
	String modid();
	String name();
	String version();
	String acceptedMinecraftVersions();
	String guiFactory();
	
}
