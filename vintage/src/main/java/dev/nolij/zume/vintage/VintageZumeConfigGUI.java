package dev.nolij.zume.vintage;

import dev.nolij.zume.impl.Zume;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.util.Collections;

import static dev.nolij.zume.impl.ZumeConstants.MOD_ID;

public class VintageZumeConfigGUI extends GuiConfig {
	
	public VintageZumeConfigGUI(GuiScreen parentScreen) {
		super(parentScreen, Collections.emptyList(), MOD_ID, false, false, "config");
		
		Zume.openConfigFile();
	}
	
	@Override
	public void initGui() {
		this.onGuiClosed();
		Minecraft.getMinecraft().displayGuiScreen(parentScreen);
	}
	
}
