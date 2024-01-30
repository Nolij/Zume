package io.github.prospector.modmenu.api;

import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

public interface ModMenuApi extends com.terraformersmc.modmenu.api.ModMenuApi {
	
	String getModId();
	
	Function<Screen, ? extends Screen> getConfigScreenFactory();
	
}
