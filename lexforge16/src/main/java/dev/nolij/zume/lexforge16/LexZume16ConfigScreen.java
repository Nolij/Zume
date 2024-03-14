package dev.nolij.zume.lexforge16;

import dev.nolij.zume.common.Zume;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;

final class LexZume16ConfigScreen {
	
	static void register() {
		ModLoadingContext.get().registerExtensionPoint(
			ExtensionPoint.CONFIGGUIFACTORY,
			() -> (minecraft, parent) -> new ConfigScreen(parent));
	}
	
	private static final class ConfigScreen extends Screen {
		
		private final Screen parent;
		
		private ConfigScreen(Screen parent) {
			super(new TextComponent(""));
			this.parent = parent;
			
			Zume.openConfigFile();
		}
		
		@Override
		protected void init() {
			Minecraft.getInstance().setScreen(parent);
		}
		
	}
	
}
