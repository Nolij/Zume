package dev.nolij.zume.primitive;

import dev.nolij.zume.api.platform.v0.CameraPerspective;
import dev.nolij.zume.api.platform.v0.IZumeImplementation;
import dev.nolij.zume.api.platform.v0.ZumeAPI;
import dev.nolij.zume.api.config.v0.ZumeConfigAPI;
import dev.nolij.zume.primitive.mixin.GameRendererAccessor;
import dev.nolij.zume.primitive.mixin.MinecraftAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.util.SmoothUtil;

public class PrimitiveZume implements ClientModInitializer, IZumeImplementation {
	
	@Override
	public void onInitializeClient() {
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT)
			return;
		
		ZumeAPI.getLogger().info("Loading Primitive Zume...");
		
		ZumeAPI.registerImplementation(this, FabricLoader.getInstance().getConfigDir());
	}
	
	@Override
	public boolean isZoomPressed() {
		//noinspection UnreachableCode
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
		//noinspection UnreachableCode
		return MinecraftAccessor.getInstance().options.thirdPerson ? CameraPerspective.THIRD_PERSON : CameraPerspective.FIRST_PERSON;
	}
	
	@Override
	public void onZoomActivate() {
		//noinspection ConstantValue
		if (ZumeConfigAPI.isCinematicZoomEnabled() && !MinecraftAccessor.getInstance().options.cinematicMode) {
			final GameRendererAccessor gameRenderer = (GameRendererAccessor) MinecraftAccessor.getInstance().field_2818;
			gameRenderer.setCinematicYawSmoother(new SmoothUtil());
			gameRenderer.setCinematicPitchSmoother(new SmoothUtil());
		}
	}
	
}
