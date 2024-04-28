package dev.nolij.zume.legacy;

import dev.nolij.zume.api.platform.v0.CameraPerspective;
import dev.nolij.zume.api.platform.v0.IZumeImplementation;
import dev.nolij.zume.api.platform.v0.ZumeAPI;
import dev.nolij.zume.api.config.v0.ZumeConfigAPI;
import dev.nolij.zume.mixin.legacy.GameRendererAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SmoothUtil;
import org.jetbrains.annotations.NotNull;

public class LegacyZume implements ClientModInitializer, IZumeImplementation {
	
	@Override
	public void onInitializeClient() {
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT)
			return;
		
		ZumeAPI.getLogger().info("Loading Legacy Zume...");
		
		ZumeAPI.registerImplementation(this, FabricLoader.getInstance().getConfigDir());
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
	public @NotNull CameraPerspective getCameraPerspective() {
		return CameraPerspective.values()[MinecraftClient.getInstance().options.perspective];
	}
	
	private static final boolean USE_CINEMATIC_CAMERA_WORKAROUND;
	
	static {
		var useWorkaround = true;
		try {
			//noinspection JavaReflectionMemberAccess
			SmoothUtil.class.getMethod("method_10852");
			useWorkaround = false;
		} catch (NoSuchMethodException ignored) { }
		
		USE_CINEMATIC_CAMERA_WORKAROUND = useWorkaround;
    }
	
	@Override
	public void onZoomActivate() {
		if (USE_CINEMATIC_CAMERA_WORKAROUND && 
			ZumeConfigAPI.isCinematicZoomEnabled() && !MinecraftClient.getInstance().options.smoothCameraEnabled) {
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
