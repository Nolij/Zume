package dev.nolij.zume.neoforge;

import dev.nolij.zume.api.platform.v0.CameraPerspective;
import dev.nolij.zume.api.platform.v0.IZumeImplementation;
import dev.nolij.zume.api.platform.v0.ZumeAPI;
import dev.nolij.zume.api.config.v0.ZumeConfigAPI;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;

import static dev.nolij.zume.impl.ZumeConstants.MOD_ID;

@Mod(MOD_ID)
public class NeoZume implements IZumeImplementation {
	
	public NeoZume(IEventBus modEventBus) {
		if (!FMLEnvironment.dist.isClient())
			return;
		
		ZumeAPI.getLogger().info("Loading NeoZume...");
		
		NeoZumeConfigScreen.register();
		
		ZumeAPI.registerImplementation(this, FMLPaths.CONFIGDIR.get());
		if (ZumeConfigAPI.isDisabled())
			return;
		
		modEventBus.addListener(this::registerKeyBindings);
		NeoForge.EVENT_BUS.addListener(this::render);
		NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::calculateFOV);
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::calculateTurnPlayerValues);
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onMouseScroll);
		NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::calculateDetachedCameraDistance);
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
	
	@Override
	public CameraPerspective getCameraPerspective() {
		return CameraPerspective.values()[Minecraft.getInstance().options.getCameraType().ordinal()];
	}
	
	private void registerKeyBindings(RegisterKeyMappingsEvent event) {
		for (final ZumeKeyBind keyBind : ZumeKeyBind.values()) {
			event.register(keyBind.value);
		}
	}
	
	private void render(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			ZumeAPI.renderHook();
		}
	}
	
	private void calculateFOV(ViewportEvent.ComputeFov event) {
		if (ZumeAPI.isFOVHookActive()) {
			event.setFOV(ZumeAPI.fovHook(event.getFOV()));
		}
	}
	
	private void calculateTurnPlayerValues(CalculatePlayerTurnEvent event) {
		event.setMouseSensitivity(ZumeAPI.mouseSensitivityHook(event.getMouseSensitivity()));
		event.setCinematicCameraEnabled(ZumeAPI.cinematicCameraEnabledHook(event.getCinematicCameraEnabled()));
	}
	
	private void onMouseScroll(InputEvent.MouseScrollingEvent event) {
		final int scrollAmount = (int) event.getScrollDeltaY();
		if (ZumeAPI.mouseScrollHook(scrollAmount)) {
			event.setCanceled(true);
		}
	}
	
	private void calculateDetachedCameraDistance(CalculateDetachedCameraDistanceEvent event) {
        event.setDistance(ZumeAPI.thirdPersonCameraHook(event.getDistance()));
	}
	
}
