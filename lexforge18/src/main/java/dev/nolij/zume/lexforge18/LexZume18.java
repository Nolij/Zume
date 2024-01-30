package dev.nolij.zume.lexforge18;

import dev.nolij.zume.common.IZumeImplementation;
import dev.nolij.zume.common.Zume;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

@Mod(Zume.MOD_ID)
public class LexZume18 implements IZumeImplementation {
	
	public LexZume18() {
		Zume.LOGGER.info("Loading LexZume18...");
		
		for (final ZumeKeyBind keyBind : ZumeKeyBind.values()) {
			ClientRegistry.registerKeyBinding(keyBind.value);
		}
		
		Zume.init(this, new File(FMLPaths.CONFIGDIR.get().toFile(), Zume.CONFIG_FILE_NAME));
		
		MinecraftForge.EVENT_BUS.addListener(this::render);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onMouseScroll);
		
		ModLoadingContext.get().registerExtensionPoint(
			ConfigGuiHandler.ConfigGuiFactory.class,
			() -> new ConfigGuiHandler.ConfigGuiFactory(((minecraft, parent) -> new Screen(new TextComponent("")) {
				@Override
				public void tick() {
					assert minecraft != null;
					
					Zume.openConfigFile();
					minecraft.setScreen(parent);
				}
			})));
	}
	
	@Override
	public boolean isZoomPressed() {
		return ZumeKeyBind.ZOOM.isPressed();
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
	
	private void onMouseScroll(InputEvent.MouseScrollEvent event) {
		final int scrollAmount = (int) event.getScrollDelta();
		if (scrollAmount != 0 &&
			Zume.interceptScroll(scrollAmount)) {
			event.setCanceled(true);
		}
	}
	
}
