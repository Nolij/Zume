package dev.nolij.zume.archaic;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.GuiConfig;
import dev.nolij.zume.common.Zume;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.util.Collections;
import java.util.Set;

public class ArchaicConfigProvider implements IModGuiFactory {
	
	public static class ArchaicZumeConfigGUI extends GuiConfig {
		
		public ArchaicZumeConfigGUI(GuiScreen parentScreen) {
			super(parentScreen, Collections.emptyList(), Zume.MOD_ID, false, false, "config");
			
			Zume.openConfigFile();
		}
		
		@Override
		public void initGui() {
			Minecraft.getMinecraft().displayGuiScreen(parentScreen);
		}
		
	}
	
	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return ArchaicZumeConfigGUI.class;
	}
	
	@Override
	public void initialize(Minecraft minecraftInstance) {
		
	}
	
	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}
	
	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
		return null;
	}
	
}
