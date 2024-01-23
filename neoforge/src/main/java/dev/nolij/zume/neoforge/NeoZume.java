package dev.nolij.zume.neoforge;

import dev.nolij.zume.common.IZumeImplementation;
import dev.nolij.zume.common.Zume;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.CalculatePlayerTurnEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;

import java.io.File;

@Mod(Zume.MOD_ID)
@OnlyIn(Dist.CLIENT)
public class NeoZume implements IZumeImplementation {
	
	public NeoZume(IEventBus modEventBus) {
		Zume.LOGGER.info("Loading NeoZume...");
		
		Zume.init(this, new File(FMLPaths.CONFIGDIR.get().toFile(), Zume.CONFIG_FILE_NAME));
		
		modEventBus.addListener(this::registerKeyBindings);
		NeoForge.EVENT_BUS.addListener(this::render);
		NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::calculateFOV);
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::calculateTurnPlayerValues);
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
	
	private void render(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			Zume.render();
		}
	}
	
	private void calculateFOV(ViewportEvent.ComputeFov event) {
		if (Zume.isFOVModified()) {
			event.setFOV(Zume.transformFOV(event.getFOV()));
		}
	}
	
	private void calculateTurnPlayerValues(CalculatePlayerTurnEvent event) {
		event.setMouseSensitivity(Zume.transformMouseSensitivity(event.getMouseSensitivity()));
		event.setCinematicCameraEnabled(Zume.transformCinematicCamera(event.getCinematicCameraEnabled()));
	}
	
	private void onMouseScroll(InputEvent.MouseScrollingEvent event) {
		final int scrollAmount = (int) event.getScrollDeltaY();
		if (scrollAmount != 0 &&
			Zume.interceptScroll(scrollAmount)) {
			event.setCanceled(true);
		}
	}
	
}
