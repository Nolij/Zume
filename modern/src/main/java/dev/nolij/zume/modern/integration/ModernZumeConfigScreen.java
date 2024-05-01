package dev.nolij.zume.modern.integration;

import dev.nolij.zume.api.platform.v0.ZumeAPI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModernZumeConfigScreen extends Screen {
	
	private final Screen parent;
	
	public ModernZumeConfigScreen(Text arg, Screen parent) {
		super(arg);
		this.parent = parent;
	}
	
	@Override
	public void init() {
		ZumeAPI.openConfigFile();
		
		MinecraftClient.getInstance().setScreen(parent);
	}
	
	@SuppressWarnings("unused")
	public void render(int mouseX, int mouseY, float delta) {
		init();
	}
	
}
