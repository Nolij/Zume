package dev.nolij.zume.legacy;

import dev.nolij.zume.common.IZumeProvider;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.legacy.mixin.GameRendererAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SmoothUtil;

public class LegacyZume implements ClientModInitializer, IZumeProvider {
	
	public static LegacyZume INSTANCE;
	
	@Override
	public void onInitializeClient() {
		INSTANCE = this;
		
		Zume.init(this, FabricLoader.getInstance().getConfigDir().resolve(Zume.CONFIG_FILE_NAME).toFile());
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
	public void onZoomActivate() {
		if (Zume.CONFIG.enableCinematicZoom && !MinecraftClient.getInstance().options.smoothCameraEnabled) {
			final GameRendererAccessor gameRenderer = (GameRendererAccessor) MinecraftClient.getInstance().gameRenderer;
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
