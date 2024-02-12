package dev.nolij.zume.legacy;

import dev.nolij.zume.common.IZumeImplementation;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.legacy.mixin.GameRendererAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SmoothUtil;

public class LegacyZume implements ClientModInitializer, IZumeImplementation {
	
	private MinecraftClient minecraftClient;
	
	@Override
	public void onInitializeClient() {
		Zume.LOGGER.info("Loading Legacy Zume...");
		
		Zume.init(this, FabricLoader.getInstance().getConfigDir().resolve(Zume.CONFIG_FILE_NAME).toFile());
		if (Zume.disabled) return;
		
		this.minecraftClient = MinecraftClient.getInstance();
	}
	
	@Override
	public boolean isZoomPressed() {
		return minecraftClient.currentScreen == null && ZumeKeyBind.ZOOM.isPressed();
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
	public void onZoomActivate() {
		if (Zume.config.enableCinematicZoom && !minecraftClient.options.smoothCameraEnabled) {
			final GameRendererAccessor gameRenderer = (GameRendererAccessor) minecraftClient.gameRenderer;
			gameRenderer.setCursorXSmoother(new SmoothUtil());
			gameRenderer.setCursorYSmoother(new SmoothUtil());
			gameRenderer.setCursorDeltaX(0F);
			gameRenderer.setCursorDeltaY(0F);
			gameRenderer.setSmoothedCursorDeltaX(0F);
			gameRenderer.setSmoothedCursorDeltaY(0F);
			gameRenderer.setLastTickDelta(0F);
		}
	}
	
}
