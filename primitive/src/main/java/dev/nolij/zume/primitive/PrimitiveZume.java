package dev.nolij.zume.primitive;

import dev.nolij.zume.common.IZumeImplementation;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.primitive.mixin.GameRendererAccessor;
import dev.nolij.zume.primitive.mixin.MinecraftAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.SmoothUtil;

public class PrimitiveZume implements ClientModInitializer, IZumeImplementation {
	
	private Minecraft minecraft;
	
	@Override
	public void onInitializeClient() {
		Zume.LOGGER.info("Loading Primitive Zume...");
		
		Zume.init(this, FabricLoader.getInstance().getConfigDir().resolve(Zume.CONFIG_FILE_NAME).toFile());
		
		this.minecraft = MinecraftAccessor.getInstance();
	}
	
	@Override
	public boolean isZoomPressed() {
		return minecraft.currentScreen == null && ZumeKeyBind.ZOOM.isPressed();
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
		if (Zume.config.enableCinematicZoom && !minecraft.options.cinematicMode) {
			final GameRendererAccessor gameRenderer = (GameRendererAccessor) minecraft.field_2818;
			gameRenderer.setCinematicYawSmoother(new SmoothUtil());
			gameRenderer.setCinematicPitchSmoother(new SmoothUtil());
		}
	}
	
}
