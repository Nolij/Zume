package dev.nolij.zume.common;

import dev.nolij.zume.common.config.ZumeConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class Zume {
	
	public static final String MOD_ID = "zume";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final String CONFIG_FILE = MOD_ID + ".json5";
	
	public static ZumeVariant ZUME_VARIANT = null;
	
	public static void calculateZumeVariant() {
		if (ZUME_VARIANT != null)
			return;
		
		if (Zume.class.getClassLoader().getResource("net/fabricmc/fabric/api/client/keybinding/v1/KeyBindingHelper.class") != null)
			ZUME_VARIANT = ZumeVariant.MODERN;
		else if (Zume.class.getClassLoader().getResource("net/legacyfabric/fabric/api/client/keybinding/v1/KeyBindingHelper.class") != null)
			ZUME_VARIANT = ZumeVariant.LEGACY;
		else if (Zume.class.getClassLoader().getResource("net/modificationstation/stationapi/api/client/event/option/KeyBindingRegisterEvent.class") != null)
			ZUME_VARIANT = ZumeVariant.PRIMITIVE;
	}
	
	private static IZumeProvider ZUME_PROVIDER;
	
	public static ZumeConfig CONFIG;
	private static double inverseSmoothness = 1D;
	
	public static void init(final IZumeProvider zumeProvider, final File configFile) {
		if (ZUME_PROVIDER != null)
			throw new AssertionError("Zume already initialized!");
		
		ZUME_PROVIDER = zumeProvider;
		
		ZumeConfig.create(configFile, config -> {
			CONFIG = config;
			inverseSmoothness = 1D / CONFIG.zoomSmoothnessMs;
		});
	}
	
	private static double fromZoom = -1D;
	private static double zoom = -1D;
	private static long tweenStart = 0L;
	
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
		if (!ZUME_PROVIDER.isZoomPressed())
			return original;
		
		final double zoom = getZoom();
		var result = original;
		
		result *= CONFIG.mouseSensitivityFloor + (zoom * (1 - CONFIG.mouseSensitivityFloor));
		
		return result;
	}
	
	private static double clamp(double value, double min, double max) {
		return Math.max(Math.min(value, max), min);
	}
	
	private static void setZoom(double targetZoom) {
		final double currentZoom = getZoom();
		tweenStart = System.currentTimeMillis();
		fromZoom = currentZoom;
		zoom = clamp(targetZoom, 0D, 1D);
	}
	
	private static void setZoomNoTween(double targetZoom) {
		tweenStart = 0L;
		fromZoom = -1D;
		zoom = clamp(targetZoom, 0D, 1D);
	}
	
	public static boolean isActive() {
		return ZUME_PROVIDER.isZoomPressed();
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
				ZUME_PROVIDER.onZoomActivate();
				zoom = 1D;
				setZoom(CONFIG.defaultZoom);
			}
			
			final long timeDelta = timestamp - prevTimestamp;
			
			if (CONFIG.enableZoomScrolling && scrollDelta != 0) {
				setZoom(zoom - scrollDelta * CONFIG.zoomSpeed * 4E-3D);
			} else if (ZUME_PROVIDER.isZoomInPressed() ^ ZUME_PROVIDER.isZoomOutPressed()) {
				final double interpolatedIncrement = CONFIG.zoomSpeed * 1E-4D * timeDelta;
				
				if (ZUME_PROVIDER.isZoomInPressed())
					setZoom(zoom - interpolatedIncrement);
				else if (ZUME_PROVIDER.isZoomOutPressed())
					setZoom(zoom + interpolatedIncrement);
			}
		}
		
		scrollDelta = 0;
		prevTimestamp = timestamp;
		wasZooming = zooming;
	}
	
}
