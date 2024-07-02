package dev.nolij.zume.legacy;

import dev.nolij.zume.api.util.v1.MethodHandleHelper;
import dev.nolij.zume.impl.CameraPerspective;
import dev.nolij.zume.impl.IZumeImplementation;
import dev.nolij.zume.impl.Zume;
import dev.nolij.zume.mixin.legacy.GameRendererAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.SmoothUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class LegacyZume implements ClientModInitializer, IZumeImplementation {
	
	@Override
	public void onInitializeClient() {
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT)
			return;
		
		Zume.LOGGER.info("Loading Legacy Zume...");
		
		Zume.registerImplementation(this, FabricLoader.getInstance().getConfigDir());
	}
	
	@Override
	public boolean isZoomPressed() {
		return MinecraftClient.getInstance().currentScreen == null && ZumeKeyBind.ZOOM.isPressed();
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
		return CameraPerspective.values()[MinecraftClient.getInstance().options.perspective];
	}
	
	private static final boolean USE_CINEMATIC_CAMERA_WORKAROUND = MethodHandleHelper.PUBLIC
		.getMethodOrNull(SmoothUtil.class, "method_10852") == null;
	
	@Override
	public void onZoomActivate() {
		if (USE_CINEMATIC_CAMERA_WORKAROUND && 
			Zume.config.enableCinematicZoom && !MinecraftClient.getInstance().options.smoothCameraEnabled) {
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
	
	private static final MethodHandle KEYBINDING_INIT_CATEGORY = 
		MethodHandleHelper.PUBLIC.getConstructorOrNull(
			KeyBinding.class, 
			MethodType.methodType(KeyBinding.class, String.class, int.class, String.class),
			String.class, int.class, String.class
		);
	private static final MethodHandle KEYBINDING_INIT_NO_CATEGORY =
		MethodHandleHelper.PUBLIC.getConstructorOrNull(
			KeyBinding.class, 
			MethodType.methodType(KeyBinding.class, String.class, int.class),
			String.class, int.class
		);
	
	public static KeyBinding newKeyBinding(String translationKey, int keyCode, String category) {
		if (KEYBINDING_INIT_CATEGORY != null)
			return (KeyBinding) KEYBINDING_INIT_CATEGORY.invokeExact(translationKey, keyCode, category);
		else
			//noinspection DataFlowIssue
			return (KeyBinding) KEYBINDING_INIT_NO_CATEGORY.invokeExact(translationKey, keyCode);
	}
	
}
