package dev.nolij.zume.primitive;

import dev.nolij.zume.common.CameraPerspective;
import dev.nolij.zume.common.IZumeImplementation;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.primitive.mixin.GameRendererAccessor;
import dev.nolij.zume.primitive.mixin.MinecraftAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.SmoothUtil;

public class PrimitiveZume implements ClientModInitializer, IZumeImplementation {
	
	@Override
	public void onInitializeClient() {
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT)
			return;
		
		Zume.LOGGER.info("Loading Primitive Zume...");
		
		Zume.init(this, FabricLoader.getInstance().getConfigDir().resolve(Zume.CONFIG_FILE_NAME).toFile());
	}
	
	@Override
	public boolean isZoomPressed() {
		return MinecraftAccessor.getInstance().currentScreen == null && ZumeKeyBind.ZOOM.isPressed();
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
		return MinecraftAccessor.getInstance().options.thirdPerson ? CameraPerspective.THIRD_PERSON : CameraPerspective.FIRST_PERSON;
	}
	
	@Override
	public void onZoomActivate() {
		//noinspection ConstantValue
		if (Zume.config.enableCinematicZoom && !MinecraftAccessor.getInstance().options.cinematicMode) {
			final GameRendererAccessor gameRenderer = (GameRendererAccessor) MinecraftAccessor.getInstance().field_2818;
			gameRenderer.setCinematicYawSmoother(new SmoothUtil());
			gameRenderer.setCinematicPitchSmoother(new SmoothUtil());
		}
	}
	
}
