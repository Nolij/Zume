package dev.nolij.zume.lexforge18;

import dev.nolij.zume.common.IZumeProvider;
import dev.nolij.zume.common.Zume;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

@Mod(Zume.MOD_ID)
public class LexZume18 implements IZumeProvider {
	
	public LexZume18() {
		Zume.LOGGER.info("Loading LexZume18...");
		
		for (final ZumeKeyBind keyBind : ZumeKeyBind.values()) {
			ClientRegistry.registerKeyBinding(keyBind.value);
		}
		
		Zume.init(this, new File(FMLPaths.CONFIGDIR.get().toFile(), Zume.CONFIG_FILE_NAME));
		
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onMouseScroll);
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
	
	private void onMouseScroll(InputEvent.MouseScrollEvent event) {
		final int scrollAmount = (int) event.getScrollDelta();
		if (scrollAmount != 0 &&
			!Zume.transformHotbarScroll(scrollAmount)) {
			event.setCanceled(true);
		}
	}
	
}
