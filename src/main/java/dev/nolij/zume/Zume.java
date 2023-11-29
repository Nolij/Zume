package dev.nolij.zume;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.MathHelper;
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
			inverseSmoothness = 1D / CONFIG.zoomSmoothnessMs;
		});
		
		for (final ZumeKeyBind keyBind : ZumeKeyBind.values()) {
			KeyBindingHelper.registerKeyBinding(keyBind.value);
		}
	}
	
	private static double fromZoom = -1D;
	private static double zoom = -1D;
	private static long tweenStart = 0L;
	private static double inverseSmoothness = 1D;
	
	private static double getZoom() {
		final long tweenLength = CONFIG.zoomSmoothnessMs;
		
		if (tweenLength != 0) {
			final long timestamp = System.currentTimeMillis();
			final long tweenEnd = tweenStart + tweenLength;
			
			if (tweenEnd >= timestamp) {
				final long delta = timestamp - tweenStart;
				final double progress = 1 - delta * inverseSmoothness;
				
				var easedProgress = progress;
				for (var i = 1; i < CONFIG.easingExponent; i++)
					easedProgress *= progress;
				easedProgress = 1 - easedProgress;
				
				return fromZoom + ((zoom - fromZoom) * easedProgress);
			}
		}
		
		return zoom;
	}
	
	public static double getFOV() {
		var zoom = getZoom();
		
		if (CONFIG.useQuadratic) {
			zoom *= zoom;
		}
		
		return CONFIG.minFOV + ((Math.max(CONFIG.maxFOV, realFOV) - CONFIG.minFOV) * zoom);
	}
	
	public static double getMouseSensitivity(double original) {
		if (!ZumeKeyBind.ZOOM.isPressed())
			return original;
		
		final double zoom = getZoom();
		var result = original;
		
		result *= CONFIG.mouseSensitivityFloor + (zoom * (1 - CONFIG.mouseSensitivityFloor));
		
		return result;
	}
	
	private static void setZoom(double targetZoom) {
		final double currentZoom = getZoom();
		tweenStart = System.currentTimeMillis();
		fromZoom = currentZoom;
		zoom = MathHelper.clamp(targetZoom, 0D, 1D);
	}
	
	private static void setZoomNoTween(double targetZoom) {
		tweenStart = 0L;
		fromZoom = -1D;
		zoom = MathHelper.clamp(targetZoom, 0D, 1D);
	}
	
	public static boolean isActive() {
		return ZumeKeyBind.ZOOM.isPressed();
	}
	
	public static int scrollDelta = 0;
	public static double realFOV = -1D;
	private static boolean wasZooming = false;
	private static long prevTimestamp;
	public static void render() {
		final long timestamp = System.currentTimeMillis();
		final boolean zooming = isActive();
		
		if (zooming) {
			if (!wasZooming) {
				zoom = 1D;
				setZoom(CONFIG.defaultZoom);
			}
			
			final long timeDelta = timestamp - prevTimestamp;
			
			if (CONFIG.enableZoomScrolling && scrollDelta != 0) {
				setZoom(zoom - scrollDelta * CONFIG.zoomSpeed * 4E-3D);
			} else if (ZumeKeyBind.ZOOM_IN.isPressed() ^ ZumeKeyBind.ZOOM_OUT.isPressed()) {
				final double interpolatedIncrement = CONFIG.zoomSpeed * 1E-4D * timeDelta;
				
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
