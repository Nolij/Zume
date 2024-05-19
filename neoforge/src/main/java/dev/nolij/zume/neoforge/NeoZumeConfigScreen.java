package dev.nolij.zume.neoforge;

import dev.nolij.zume.api.platform.v1.ZumeAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class NeoZumeConfigScreen extends Screen {
	
	private final Screen parent;
	
	public NeoZumeConfigScreen(Screen parent) {
		super(Component.empty());
		this.parent = parent;
	}
	
	@Override
	public void init() {
		ZumeAPI.openConfigFile();
		Minecraft.getInstance().setScreen(parent);
	}
	
}
