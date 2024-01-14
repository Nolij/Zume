package dev.nolij.zume.primitive;

import dev.nolij.zume.common.IZumeProvider;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.primitive.mixin.GameRendererAccessor;
import dev.nolij.zume.primitive.mixin.MinecraftAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.util.SmoothUtil;

public class PrimitiveZume implements ClientModInitializer, IZumeProvider {
	
	@Override
	public void onInitializeClient() {
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
		//noinspection ConstantValue
		if (Zume.CONFIG.enableCinematicZoom && !MinecraftAccessor.getInstance().options.cinematicMode) {
			final GameRendererAccessor gameRenderer = (GameRendererAccessor) MinecraftAccessor.getInstance().field_2818;
			gameRenderer.setCinematicYawSmoother(new SmoothUtil());
			gameRenderer.setCinematicPitchSmoother(new SmoothUtil());
		}
	}
	
}
