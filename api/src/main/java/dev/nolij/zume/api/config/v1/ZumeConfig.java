package dev.nolij.zume.api.config.v1;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public final class ZumeConfig {
	
	public boolean isCinematicZoomEnabled;
	public double mouseSensitivityFloor;
	public short zoomSpeed;
	public boolean isZoomScrollingEnabled;
	public short zoomSmoothnessMilliseconds;
	public double animationEasingExponent;
	public double zoomEasingExponent;
	public double defaultZoom;
	public boolean isFirstPersonToggleModeEnabled;
	public boolean isThirdPersonToggleModeEnabled;
	public double minimumFOV;
	public double maximumThirdPersonZoomBlocks;
	public double minimumThirdPersonZoomBlocks;
	public boolean isDisabled;
	
}
