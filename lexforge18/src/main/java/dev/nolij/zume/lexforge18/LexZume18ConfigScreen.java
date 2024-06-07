package dev.nolij.zume.lexforge18;

import dev.nolij.zume.impl.Zume;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.ModLoadingContext;

final class LexZume18ConfigScreen {
	
	static void register() {
		ModLoadingContext.get().registerExtensionPoint(
			ConfigGuiHandler.ConfigGuiFactory.class,
			() -> new ConfigGuiHandler.ConfigGuiFactory((minecraft, parent) -> new Screen(new TextComponent("")) {
				@Override
				public void tick() {
					Zume.openConfigFile();
					Minecraft.getInstance().setScreen(parent);
				}
			}));
	}
	
}
