package dev.nolij.zume;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_555;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.SmoothUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Zume implements ClientModInitializer {
	
	public static final String MOD_ID = "zume";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	private static final String CONFIG_FILE = MOD_ID + ".json5";
	
	public static ZumeConfig CONFIG;
	
	@Override
	public void onInitializeClient() {
		ZumeConfig.create(FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE).toFile(), config -> {
			CONFIG = config;
			inverseSmoothness = 1F / CONFIG.zoomSmoothnessMs;
		});
	}
	
	private static float fromZoom = -1F;
	private static float zoom = -1F;
	private static long tweenStart = 0L;
	private static float inverseSmoothness = 1F;
	
	private static float getZoom() {
		final long tweenLength = CONFIG.zoomSmoothnessMs;
		
		if (tweenLength != 0) {
			final long timestamp = System.currentTimeMillis();
			final long tweenEnd = tweenStart + tweenLength;
			
			if (tweenEnd >= timestamp) {
				final long delta = timestamp - tweenStart;
				final float progress = 1 - delta * inverseSmoothness;
				
				var easedProgress = progress;
				for (var i = 1; i < CONFIG.easingExponent; i++)
					easedProgress *= progress;
				easedProgress = 1 - easedProgress;
				
				return fromZoom + ((zoom - fromZoom) * easedProgress);
			}
		}
		
		return zoom;
	}
	
	public static float getFOV() {
		var zoom = getZoom();
		
		if (CONFIG.useQuadratic) {
			zoom *= zoom;
		}
		
		return CONFIG.minFOV + ((Math.max(CONFIG.maxFOV, realFOV) - CONFIG.minFOV) * zoom);
	}
	
	public static float getMouseSensitivity(float original) {
		if (!ZumeKeyBind.ZOOM.isPressed())
			return original;
		
		final float zoom = getZoom();
		var result = original;
		
		result *= CONFIG.mouseSensitivityFloor + (zoom * (1 - CONFIG.mouseSensitivityFloor));
		
		return result;
	}
	
	private static float clamp(float value, float min, float max) {
		return Math.max(Math.min(value, max), min);
	}
	
	private static void setZoom(float targetZoom) {
		final float currentZoom = getZoom();
		tweenStart = System.currentTimeMillis();
		fromZoom = currentZoom;
		zoom = clamp(targetZoom, 0F, 1F);
	}
	
	private static void setZoomNoTween(float targetZoom) {
		tweenStart = 0L;
		fromZoom = -1F;
		zoom = clamp(targetZoom, 0F, 1F);
	}
	
	public static boolean isActive() {
		return ZumeKeyBind.ZOOM.isPressed();
	}
	
	public static int scrollDelta = 0;
	public static float realFOV = -1F;
	private static boolean wasZooming = false;
	private static long prevTimestamp;
	public static void render() {
		final long timestamp = System.currentTimeMillis();
		final boolean zooming = isActive();
		
		if (zooming) {
			if (!wasZooming) {
				if (CONFIG.enableCinematicZoom && !Minecraft.INSTANCE.options.cinematicMode) {
					final class_555 class555 = Minecraft.INSTANCE.field_2818;
					class555.field_2353 = new SmoothUtil();
					class555.field_2354 = new SmoothUtil();
				}
				zoom = 1F;
				setZoom(CONFIG.defaultZoom);
			}
			
			final long timeDelta = timestamp - prevTimestamp;
			
			if (CONFIG.enableZoomScrolling && scrollDelta != 0) {
				setZoom(zoom - scrollDelta * CONFIG.zoomSpeed * 4E-3F);
			} else if (ZumeKeyBind.ZOOM_IN.isPressed() ^ ZumeKeyBind.ZOOM_OUT.isPressed()) {
				final float interpolatedIncrement = CONFIG.zoomSpeed * 1E-4F * timeDelta;
				
				if (ZumeKeyBind.ZOOM_IN.isPressed())
					setZoom(zoom - interpolatedIncrement);
				else if (ZumeKeyBind.ZOOM_OUT.isPressed())
					setZoom(zoom + interpolatedIncrement);
			}
		}
		
		scrollDelta = 0;
		prevTimestamp = timestamp;
		wasZooming = zooming;
	}
	
}
