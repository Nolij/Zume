package dev.nolij.zume.api.config.v0;

import dev.nolij.zume.impl.Zume;
import dev.nolij.zume.impl.config.ZumeConfigImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public final class ZumeConfigAPI {
	
	@Contract(pure = true)
	public static boolean isCinematicZoomEnabled() {
		return Zume.config.enableCinematicZoom;
	}
	
	@Contract(pure = true)
	public static double getMouseSensitivityFloor() {
		return Zume.config.mouseSensitivityFloor;
	}
	
	@Contract(pure = true)
	public static short getZoomSpeed() {
		return Zume.config.zoomSpeed;
	}
	
	@Contract(pure = true)
	public static boolean isZoomScrollingEnabled() {
		return Zume.config.enableZoomScrolling;
	}
	
	@Contract(pure = true)
	public static short getZoomSmoothnessMilliseconds() {
		return Zume.config.zoomSmoothnessMs;
	}
	
	@Contract(pure = true)
	public static double getAnimationEasingExponent() {
		return Zume.config.animationEasingExponent;
	}
	
	@Contract(pure = true)
	public static double getZoomEasingExponent() {
		return Zume.config.zoomEasingExponent;
	}
	
	@Contract(pure = true)
	public static double getDefaultZoom() {
		return Zume.config.defaultZoom;
	}
	
	@Contract(pure = true)
	public static boolean isFirstPersonToggleModeEnabled() {
		return Zume.config.toggleMode;
	}
	
	@Contract(pure = true)
	public static boolean isThirdPersonToggleModeEnabled() {
		return Zume.config.thirdPersonToggleMode;
	}
	
	@Contract(pure = true)
	public static double getMinimumFOV() {
		return Zume.config.minFOV;
	}
	
	@Contract(pure = true)
	public static double getMaximumThirdPersonZoomBlocks() {
		return Zume.config.maxThirdPersonZoomDistance;
	}
	
	@Contract(pure = true)
	public static double getMinimumThirdPersonZoomBlocks() {
		return Zume.config.minThirdPersonZoomDistance;
	}
	
	@Contract(pure = true)
	public static boolean isDisabled() {
		return Zume.config.disable;
	}
	
	public static @NotNull ZumeConfig getSnapshot() {
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
	
	public static void replaceConfig(@NotNull ZumeConfig replacement) throws InterruptedException {
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
