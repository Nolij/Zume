package dev.nolij.zume.neoforge;

import dev.nolij.zume.common.CameraPerspective;
import dev.nolij.zume.common.IZumeImplementation;
import dev.nolij.zume.common.Zume;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;

import java.io.File;

@Mod(Zume.MOD_ID)
public class NeoZume implements IZumeImplementation {
	
	private final Minecraft minecraft;
	
	public NeoZume(IEventBus modEventBus) {
		Zume.LOGGER.info("Loading NeoZume...");
		
		this.minecraft = Minecraft.getInstance();
		
		ModLoadingContext.get().registerExtensionPoint(
			ConfigScreenHandler.ConfigScreenFactory.class,
			() -> new ConfigScreenHandler.ConfigScreenFactory(((minecraft, parent) -> new Screen(Component.empty()) {
				@Override
				public void init() {
					assert minecraft != null;
					
					Zume.openConfigFile();
					minecraft.setScreen(parent);
				}
			})));
		
		Zume.init(this, new File(FMLPaths.CONFIGDIR.get().toFile(), Zume.CONFIG_FILE_NAME));
		if (Zume.disabled) return;
		
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
		return CameraPerspective.values()[minecraft.options.getCameraType().ordinal()];
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
		if (Zume.shouldHookFOV()) {
			event.setFOV(Zume.transformFOV(event.getFOV()));
		}
	}
	
	private void calculateTurnPlayerValues(CalculatePlayerTurnEvent event) {
		event.setMouseSensitivity(Zume.transformMouseSensitivity(event.getMouseSensitivity()));
		event.setCinematicCameraEnabled(Zume.transformCinematicCamera(event.getCinematicCameraEnabled()));
	}
	
	private void onMouseScroll(InputEvent.MouseScrollingEvent event) {
		final int scrollAmount = (int) event.getScrollDeltaY();
		if (Zume.interceptScroll(scrollAmount)) {
			event.setCanceled(true);
		}
	}
	
	private void calculateDetachedCameraDistance(CalculateDetachedCameraDistanceEvent event) {
		if (Zume.shouldHook()) {
			event.setDistance(Zume.transformThirdPersonDistance(event.getDistance()));
		}
	}
	
}
