package dev.nolij.zume.archaic;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.GuiConfig;
import dev.nolij.zume.api.platform.v1.ZumeAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.util.Collections;
import java.util.Set;

import static dev.nolij.zume.impl.ZumeConstants.MOD_ID;

@SuppressWarnings("unused")
public class ArchaicConfigProvider implements IModGuiFactory {
	
	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return ArchaicZumeConfigGUI.class;
	}
	
	@Override
	public void initialize(Minecraft minecraftInstance) {}
	
	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}
	
	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
		return null;
	}
	
}
