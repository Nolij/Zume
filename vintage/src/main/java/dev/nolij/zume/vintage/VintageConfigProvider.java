package dev.nolij.zume.vintage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.Set;

@SuppressWarnings("unused")
public class VintageConfigProvider implements IModGuiFactory {
	
	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
		return new VintageZumeConfigGUI(parentScreen);
	}
	
	@Override
	public boolean hasConfigGui() {
		return true;
	}
	
	@Override
	public void initialize(Minecraft minecraftInstance) {}
	
	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}
	
}
