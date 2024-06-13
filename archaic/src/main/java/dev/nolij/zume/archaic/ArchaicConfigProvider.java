package dev.nolij.zume.archaic;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.gui.ForgeGuiFactory;

@SuppressWarnings("unused")
public class ArchaicConfigProvider extends ForgeGuiFactory {
	
	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return ArchaicZumeConfigGUI.class;
	}
	
}
