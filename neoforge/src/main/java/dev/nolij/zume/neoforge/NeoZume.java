package dev.nolij.zume.neoforge;

import dev.nolij.zume.common.IZumeProvider;
import dev.nolij.zume.common.Zume;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.io.File;

@Mod(Zume.MOD_ID)
@OnlyIn(Dist.CLIENT)
public class NeoZume implements IZumeProvider {
	
	public static NeoZume INSTANCE;
	
	public NeoZume(IEventBus modEventBus) {
		INSTANCE = this;
		
		Zume.LOGGER.info("Loading NeoZume...");
		
		Zume.init(this, new File(FMLPaths.CONFIGDIR.get().toFile(), Zume.CONFIG_FILE_NAME));
		
		modEventBus.addListener(this::registerKeyBindings);
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onMouseScroll);
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
	
	private void registerKeyBindings(RegisterKeyMappingsEvent event) {
		for (final ZumeKeyBind keyBind : ZumeKeyBind.values()) {
			event.register(keyBind.value);
		}
	}
	
	private void onMouseScroll(InputEvent.MouseScrollingEvent event) {
		final int scrollAmount = (int) event.getScrollDeltaY();
		if (scrollAmount != 0 &&
			!Zume.transformHotbarScroll(scrollAmount)) {
			event.setCanceled(true);
		}
	}
	
}
