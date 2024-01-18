package dev.nolij.zume.lexforge;

import dev.nolij.zume.common.IZumeProvider;
import dev.nolij.zume.common.Zume;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

@Mod(Zume.MOD_ID)
@OnlyIn(Dist.CLIENT)
public class LexZume implements IZumeProvider {
	
	public LexZume() {
		Zume.LOGGER.info("Loading LexZume...");
		
		Zume.init(this, new File(FMLPaths.CONFIGDIR.get().toFile(), Zume.CONFIG_FILE_NAME));
		
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerKeyBindings);
		MinecraftForge.EVENT_BUS.addListener(this::render);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::calculateFOV);
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
	
	private void registerKeyBindings(RegisterKeyMappingsEvent event) {
		for (final ZumeKeyBind keyBind : ZumeKeyBind.values()) {
			event.register(keyBind.value);
		}
	}
	
	private void render(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			Zume.render();
		}
	}
	
	private void calculateFOV(ViewportEvent.ComputeFov event) {
		if (Zume.isZooming()) {
			event.setFOV(Zume.transformFOV(event.getFOV()));
		}
	}
	
	private void onMouseScroll(InputEvent.MouseScrollingEvent event) {
		final int scrollAmount = (int) event.getScrollDelta();
		if (scrollAmount != 0 &&
			!Zume.transformHotbarScroll(scrollAmount)) {
			event.setCanceled(true);
		}
	}
	
}
