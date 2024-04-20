package dev.nolij.zume.neoforge;

import dev.nolij.zume.api.platform.v0.ZumeAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.ConfigScreenHandler;

final class NeoZumeConfigScreen {
	
	static void register() {
		ModLoadingContext.get().registerExtensionPoint(
			ConfigScreenHandler.ConfigScreenFactory.class,
			() -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new Screen(Component.empty()) {
				@Override
				public void init() {
					ZumeAPI.openConfigFile();
					Minecraft.getInstance().setScreen(parent);
				}
			}));
	}
	
}
