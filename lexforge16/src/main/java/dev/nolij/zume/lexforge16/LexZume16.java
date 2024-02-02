package dev.nolij.zume.lexforge16;

import dev.nolij.zume.common.IZumeImplementation;
import dev.nolij.zume.common.Zume;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

@Mod(Zume.MOD_ID)
public class LexZume16 implements IZumeImplementation {
	
	private final Minecraft minecraft;
	
	public LexZume16() {
		Zume.LOGGER.info("Loading LexZume16...");
		
		for (final ZumeKeyBind keyBind : ZumeKeyBind.values()) {
			ClientRegistry.registerKeyBinding(keyBind.value);
		}
		
		Zume.init(this, new File(FMLPaths.CONFIGDIR.get().toFile(), Zume.CONFIG_FILE_NAME));
		
		MinecraftForge.EVENT_BUS.addListener(this::render);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::calculateFOV);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onMouseScroll);
		
		ModLoadingContext.get().registerExtensionPoint(
			ExtensionPoint.CONFIGGUIFACTORY,
			() -> (minecraft, parent) -> new ConfigScreen(new TextComponent(""), minecraft, parent));
		
		this.minecraft = Minecraft.getInstance();
	}
	
	private static final class ConfigScreen extends Screen {
		
		private final Minecraft minecraft;
		private final Screen parent;
		
		private ConfigScreen(Component title, Minecraft minecraft, Screen parent) {
			super(title);
			this.minecraft = minecraft;
			this.parent = parent;
			
			Zume.openConfigFile();
		}
		
		@Override
		protected void init() {
			minecraft.setScreen(parent);
		}
		
	}
	
	@Override
	public boolean isZoomPressed() {
		return minecraft.screen == null && ZumeKeyBind.ZOOM.isPressed();
	}
	
	@Override
	public boolean isZoomInPressed() {
		return ZumeKeyBind.ZOOM_IN.isPressed();
	}
	
	@Override
	public boolean isZoomOutPressed() {
		return ZumeKeyBind.ZOOM_OUT.isPressed();
	}
	
	private void render(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			Zume.render();
		}
	}
	
	private void calculateFOV(EntityViewRenderEvent.FOVModifier event) {
		if (Zume.isFOVModified()) {
			event.setFOV(Zume.transformFOV(event.getFOV()));
		}
	}
	
	private void onMouseScroll(InputEvent.MouseScrollEvent event) {
		final int scrollAmount = (int) event.getScrollDelta();
		if (scrollAmount != 0 &&
			Zume.interceptScroll(scrollAmount)) {
			event.setCanceled(true);
		}
	}
	
}
