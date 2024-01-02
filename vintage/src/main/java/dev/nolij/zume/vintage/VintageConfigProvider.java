package dev.nolij.zume.vintage;

import dev.nolij.zume.common.Zume;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public class VintageConfigProvider implements IModGuiFactory {
	
	public static class VintageZumeConfigGUI extends GuiConfig {
		
		public VintageZumeConfigGUI(GuiScreen parentScreen) throws IOException {
			super(parentScreen, Collections.emptyList(), Zume.MOD_ID, false, false, "config");
			
			Zume.openConfigFile();
		}
		
		@Override
		public void initGui() {
			this.onGuiClosed();
			Minecraft.getMinecraft().displayGuiScreen(parentScreen);
		}
		
	}
	
	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
		try {
			return new VintageZumeConfigGUI(parentScreen);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}
	
	@Override
	public boolean hasConfigGui() {
		return true;
	}
	
	@Override
	public void initialize(Minecraft minecraftInstance) {
		
	}
	
	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}
	
}
