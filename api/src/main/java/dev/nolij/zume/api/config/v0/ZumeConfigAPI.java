package dev.nolij.zume.api.config.v0;

import dev.nolij.zume.impl.Zume;
import dev.nolij.zume.impl.config.ZumeConfigImpl;

public final class ZumeConfigAPI {
	
	public static boolean isCinematicZoomEnabled() {
		return Zume.config.enableCinematicZoom;
	}
	
	public static double getMouseSensitivityFloor() {
		return Zume.config.mouseSensitivityFloor;
	}
	
	public static short getZoomSpeed() {
		return Zume.config.zoomSpeed;
	}
	
	public static boolean isZoomScrollingEnabled() {
		return Zume.config.enableZoomScrolling;
	}
	
	public static short getZoomSmoothnessMilliseconds() {
		return Zume.config.zoomSmoothnessMs;
	}
	
	public static double getAnimationEasingExponent() {
		return Zume.config.animationEasingExponent;
	}
	
	public static double getZoomEasingExponent() {
		return Zume.config.zoomEasingExponent;
	}
	
	public static double getDefaultZoom() {
		return Zume.config.defaultZoom;
	}
	
	public static boolean isFirstPersonToggleModeEnabled() {
		return Zume.config.toggleMode;
	}
	
	public static boolean isThirdPersonToggleModeEnabled() {
		return Zume.config.thirdPersonToggleMode;
	}
	
	public static double getMinimumFOV() {
		return Zume.config.minFOV;
	}
	
	public static double getMaximumThirdPersonZoomBlocks() {
		return Zume.config.maxThirdPersonZoomDistance;
	}
	
	public static double getMinimumThirdPersonZoomBlocks() {
		return Zume.config.minThirdPersonZoomDistance;
	}
	
	public static boolean isDisabled() {
		return Zume.config.disable;
	}
	
	public static ZumeConfig getSnapshot() {
		final ZumeConfig snapshot = new ZumeConfig();
		
		snapshot.isCinematicZoomEnabled = isCinematicZoomEnabled();
		snapshot.mouseSensitivityFloor = getMouseSensitivityFloor();
		snapshot.zoomSpeed = getZoomSpeed();
		snapshot.isZoomScrollingEnabled = isZoomScrollingEnabled();
		snapshot.zoomSmoothnessMilliseconds = getZoomSmoothnessMilliseconds();
		snapshot.animationEasingExponent = getAnimationEasingExponent();
		snapshot.zoomEasingExponent = getZoomEasingExponent();
		snapshot.defaultZoom = getDefaultZoom();
		snapshot.isFirstPersonToggleModeEnabled = isFirstPersonToggleModeEnabled();
		snapshot.isThirdPersonToggleModeEnabled = isThirdPersonToggleModeEnabled();
		snapshot.minimumFOV = getMinimumFOV();
		snapshot.maximumThirdPersonZoomBlocks = getMaximumThirdPersonZoomBlocks();
		snapshot.minimumThirdPersonZoomBlocks = getMinimumThirdPersonZoomBlocks();
		snapshot.isDisabled = isDisabled();
		
		return snapshot;
	}
	
	public static void replaceConfig(ZumeConfig replacement) throws InterruptedException {
		final ZumeConfigImpl config = new ZumeConfigImpl();
		
		config.enableCinematicZoom = replacement.isCinematicZoomEnabled;
		config.mouseSensitivityFloor = replacement.mouseSensitivityFloor;
		config.zoomSpeed = replacement.zoomSpeed;
		config.enableZoomScrolling = replacement.isZoomScrollingEnabled;
		config.zoomSmoothnessMs = replacement.zoomSmoothnessMilliseconds;
		config.animationEasingExponent = replacement.animationEasingExponent;
		config.zoomEasingExponent = replacement.zoomEasingExponent;
		config.defaultZoom = replacement.defaultZoom;
		config.toggleMode = replacement.isFirstPersonToggleModeEnabled;
		config.thirdPersonToggleMode = replacement.isThirdPersonToggleModeEnabled;
		config.minFOV = replacement.minimumFOV;
		config.maxThirdPersonZoomDistance = replacement.maximumThirdPersonZoomBlocks;
		config.minThirdPersonZoomDistance = replacement.minimumThirdPersonZoomBlocks;
		config.disable = replacement.isDisabled;
		
		ZumeConfigImpl.replace(config);
	}
	
}
