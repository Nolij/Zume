package dev.nolij.zume.archaic;

import net.minecraftforge.client.gui.ForgeGuiFactory;
import net.minecraft.client.gui.GuiScreen;


@SuppressWarnings("unused")
public class ArchaicConfigProvider extends ForgeGuiFactory {
	
	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return ArchaicZumeConfigGUI.class;
	}
	
}
