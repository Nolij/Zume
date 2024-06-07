package dev.nolij.zume.impl;

import dev.nolij.zume.api.util.v1.MathHelper;
import dev.nolij.zume.impl.config.ZumeConfigImpl;
import dev.nolij.zume.api.util.v1.EasingHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static dev.nolij.zume.impl.ZumeConstants.MOD_ID;

public class Zume {
	
	//region Constants
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final String CONFIG_FILE_NAME = MOD_ID + ".json5";
	//endregion
	
	//region Platform Detection
	public static final HostPlatform HOST_PLATFORM;
	
	static {
		final String OS_NAME = System.getProperty("os.name").toLowerCase();
		
		if (OS_NAME.contains("linux"))
			HOST_PLATFORM = HostPlatform.LINUX;
		else if (OS_NAME.contains("mac"))
			HOST_PLATFORM = HostPlatform.MAC_OS;
		else if (OS_NAME.contains("win"))
			HOST_PLATFORM = HostPlatform.WINDOWS;
		else
			HOST_PLATFORM = HostPlatform.UNKNOWN;
	}
	//endregion
	
	//region Public Members
	public static IZumeImplementation implementation;
	public static ZumeConfigImpl config;
	public static boolean disabled = false;
	//endregion
	
	//region Private Members
	private static final EasedDouble zoom = new EasedDouble(1D);
	private static int scrollDelta = 0;
	private static boolean wasHeld = false;
	private static boolean zooming = false;
	private static boolean wasZooming = false;
	private static long prevRenderTimestamp;
	//endregion
	
	//region Initialization Methods
	public static void registerImplementation(final IZumeImplementation implementation, final Path instanceConfigPath) {
		if (Zume.implementation != null)
			throw new AssertionError("Zume already initialized!");
		
		Zume.implementation = implementation;
		
		ZumeConfigImpl.init(instanceConfigPath, CONFIG_FILE_NAME, config -> {
			Zume.config = config;
			zoom.update(config.zoomSmoothnessMs, config.animationEasingExponent);
		});
		
		disabled = config.disable;
	}
	//endregion
	
	//region Zoom Mutation Methods
	private static double getZoom() {
		return zoom.getEased();
	}
	
	private static void setZoom(final double targetZoom) {
		zoom.set(MathHelper.clamp(targetZoom, 0D, 1D));
	}
	
	private static void setZoom(final double fromZoom, final double targetZoom) {
		zoom.set(MathHelper.clamp(fromZoom, 0D, 1D), MathHelper.clamp(targetZoom, 0D, 1D));
	}
	
	private static double getThirdPersonStartZoom() {
		return EasingHelper.inverseOut(
			config.minThirdPersonZoomDistance, config.maxThirdPersonZoomDistance, 
			4D, config.zoomEasingExponent);
	}
	
	private static void onZoomActivate() {
		implementation.onZoomActivate();
		
		if (shouldUseFirstPersonZoom())
			setZoom(1D, 1 - config.defaultZoom);
		else {
			final double from = getThirdPersonStartZoom();
			final double target;
			
			if (implementation.getCameraPerspective() == CameraPerspective.THIRD_PERSON)
				target = EasingHelper.linear(1D, from, config.defaultZoom);
			else
				target = EasingHelper.linear(from, 0D, config.defaultZoom);
			
			setZoom(from, target);
		}
	}
	
	private static void onZoomDeactivate() {
		if (shouldUseFirstPersonZoom())
			setZoom(1D);
		else
			setZoom(getThirdPersonStartZoom());
	}
	//endregion
	
	//region API Methods
	public static void openConfigFile() {
		final File configFile = ZumeConfigImpl.getConfigFile();
		try {
			try {
				Desktop.getDesktop().open(configFile);
			} catch (HeadlessException ignored) {
				final String CONFIG_PATH = configFile.getCanonicalPath();
				
				final ProcessBuilder builder = new ProcessBuilder().inheritIO();
				
				switch (HOST_PLATFORM) {
					case LINUX, UNKNOWN -> builder.command("xdg-open", CONFIG_PATH);
					case WINDOWS -> builder.command("rundll32", "url.dll,FileProtocolHandler", CONFIG_PATH);
					case MAC_OS -> builder.command("open", "-t", CONFIG_PATH);
				}
				
				builder.start();
			}
		} catch (IOException e) {
			Zume.LOGGER.error("Error opening config file: ", e);
		}
	}
	
	public static double fovHook(final double original) {
		return EasingHelper.out(config.minFOV, original, getZoom(), config.zoomEasingExponent);
	}
	
	public static double thirdPersonCameraHook(final double original) {
		if (shouldUseFirstPersonZoom() || !shouldHook())
			return original;
		
		return original * 0.25D * EasingHelper.out(config.minThirdPersonZoomDistance, config.maxThirdPersonZoomDistance, getZoom(), config.zoomEasingExponent);
	}
	
	public static boolean cinematicCameraEnabledHook(final boolean original) {
		if (Zume.config.enableCinematicZoom && isActive())
			return true;
		
		return original;
	}
	
	public static double mouseSensitivityHook(final double original) {
		if (!isActive() || !shouldUseFirstPersonZoom())
			return original;
		
		return original * EasingHelper.out(config.mouseSensitivityFloor, 1D, getZoom(), 1);
	}
	
	public static boolean isMouseScrollHookActive() {
		return Zume.config.enableZoomScrolling && isActive();
	}
	
	public static boolean mouseScrollHook(final int scrollDelta) {
        if (!isMouseScrollHookActive() || scrollDelta == 0)
            return false;
		
        Zume.scrollDelta += MathHelper.sign(scrollDelta);
        return true;
    }
	
	private static boolean getToggleMode() {
		return shouldUseFirstPersonZoom()
		       ? config.toggleMode
		       : config.thirdPersonToggleMode;
	}
	
	public static boolean isActive() {
		if (disabled || implementation == null)
			return false;
		
		return zooming;
	}
	
	private static boolean shouldHook() {
		if (disabled || implementation == null)
			return false;
		
		return isActive() || zoom.isEasing();
	}
	
	private static boolean shouldUseFirstPersonZoom() {
		return config.maxThirdPersonZoomDistance == 0 || 
			implementation.getCameraPerspective() == CameraPerspective.FIRST_PERSON;
	}
	
	public static boolean isFOVHookActive() {
		return shouldHook() && shouldUseFirstPersonZoom();
	}
	
	public static void renderHook() {
		if (disabled || implementation == null)
			return;
		
		final long timestamp = System.currentTimeMillis();
		final boolean held = implementation.isZoomPressed();
		final boolean toggleMode = getToggleMode();
		
		if (toggleMode && held && !wasHeld)
			zooming = !zooming;
		else if (!toggleMode)
			zooming = held;
		
		if (zooming) {
			if (!wasZooming)
				onZoomActivate();
			
			final long timeDelta = timestamp - prevRenderTimestamp;
			
			if (config.enableZoomScrolling && scrollDelta != 0) {
				setZoom(zoom.getTarget() - (scrollDelta * config.zoomSpeed * 4E-3D));
			} else if (implementation.isZoomInPressed() ^ implementation.isZoomOutPressed()) {
				final double interpolatedIncrement = config.zoomSpeed * 1E-4D * timeDelta;
				
				if (implementation.isZoomInPressed())
					setZoom(zoom.getTarget() - interpolatedIncrement);
				else if (implementation.isZoomOutPressed())
					setZoom(zoom.getTarget() + interpolatedIncrement);
			}
		} else if (wasZooming) {
			onZoomDeactivate();
		}
		
		scrollDelta = 0;
		prevRenderTimestamp = timestamp;
		wasHeld = held;
		wasZooming = zooming;
	}
	//endregion
	
}
