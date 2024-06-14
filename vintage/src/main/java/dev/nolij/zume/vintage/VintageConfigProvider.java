package dev.nolij.zume.vintage;

import dev.nolij.zumegradle.proguard.ProGuardKeep;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.gui.ForgeGuiFactory;

@SuppressWarnings("unused")
public class VintageConfigProvider extends ForgeGuiFactory {
	
	@ProGuardKeep
	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
		return new VintageZumeConfigGUI(parentScreen);
	}
	
}
