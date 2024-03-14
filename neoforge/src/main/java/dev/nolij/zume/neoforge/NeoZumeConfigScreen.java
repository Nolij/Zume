package dev.nolij.zume.neoforge;

import dev.nolij.zume.common.Zume;
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
					Zume.openConfigFile();
					Minecraft.getInstance().setScreen(parent);
				}
			}));
	}
	
}
