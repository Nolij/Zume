package io.github.prospector.modmenu.api;

import dev.nolij.zumegradle.proguard.ProGuardKeep;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Function;

@ProGuardKeep
public interface ModMenuApi extends com.terraformersmc.modmenu.api.ModMenuApi {
	
	String getModId();
	
	Function<Screen, ? extends Screen> getConfigScreenFactory();
	
}
