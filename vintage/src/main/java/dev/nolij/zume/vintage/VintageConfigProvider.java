package dev.nolij.zume.vintage;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.gui.ForgeGuiFactory;

@SuppressWarnings("unused")
public class VintageConfigProvider extends ForgeGuiFactory {
	
	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
		return new VintageZumeConfigGUI(parentScreen);
	}
	
}
