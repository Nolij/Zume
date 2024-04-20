package dev.nolij.zume.api.platform.v0;

import dev.nolij.zume.impl.CameraPerspective;
import dev.nolij.zume.impl.Zume;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

public final class ZumeAPI {
	
	public static Logger getLogger() {
		return Zume.LOGGER;
	}
	
	/**
	 * Attempts to open Zume's config file in the system's text editor.
	 */
	public static void openConfigFile() {
		Zume.openConfigFile();
	}
	
	/**
	 * Invoke this in the initializer of your Zume implementation.
	 *
	 * @param implementation The {@linkplain IZumeImplementation} Zume should use. 
	 * @param instanceConfigPath The {@linkplain Path} Zume should use for storing the instance-specific config.
	 */
	public static void registerImplementation(IZumeImplementation implementation, Path instanceConfigPath) {
		Zume.init(new dev.nolij.zume.impl.IZumeImplementation() {
			@Override
			public boolean isZoomPressed() {
				return implementation.isZoomPressed();
			}
			
			@Override
			public boolean isZoomInPressed() {
				return implementation.isZoomInPressed();
			}
			
			@Override
			public boolean isZoomOutPressed() {
				return implementation.isZoomOutPressed();
			}
			
			@Override
			public CameraPerspective getCameraPerspective() {
				return CameraPerspective.values()[implementation.getCameraPerspective().ordinal()];
			}
			
			@Override
			public void onZoomActivate() {
				implementation.onZoomActivate();
			}
		}, instanceConfigPath);
	}
	
	//region Query Methods
	/**
	 * Returns `true` if Zoom is activated.
	 */
	public static boolean isActive() {
		return Zume.isEnabled();
	}
	
	/**
	 * Returns `true` if FOV should be hooked.
	 */
	public static boolean isFOVHookActive() {
		return Zume.shouldHookFOV();
	}
	
	/**
	 * Returns `true` if mouse scrolling should be hooked.
	 */
	public static boolean isMouseScrollHookActive() {
		return Zume.shouldCancelScroll();
	}
	//endregion
	
	//region Hooks
	/**
	 * This should be invoked once at the beginning of every frame. 
	 * It will handle Keybindings and Zoom Scrolling if the other hooks in this API are used correctly.
	 */
	public static void renderHook() {
		Zume.render();
	}
	
	/**
	 * ONLY INVOKE THIS METHOD WHEN {@linkplain ZumeAPI#isFOVHookActive()} RETURNS `true`. 
	 * That check was explicitly excluded from this method for efficiency and for mixin compatibility.
	 * The {@linkplain IZumeImplementation} is responsible for this check.
	 *
	 * @param fov The unmodified FOV value
	 * {@return The new FOV transformed by Zume}
	 */
	public static double fovHook(double fov) {
		return Zume.transformFOV(fov);
	}
	
	/**
	 * This method assumes Zume is active and the camera perspective is third-person. 
	 * If it is not, using this value will cause bugs.
	 *
	 * @param distance The vanilla third-person camera distance
	 * @return The new third-person camera distance
	 */
	public static double thirdPersonCameraHook(double distance) {
		return Zume.transformThirdPersonDistance(distance);
	}
	
	/**
	 * The return value of this method can be safely used without checking whether Zume is active.
	 *
	 * @param mouseSensitivity The unmodified mouse sensitivity
	 * {@return The new mouse sensitivity, transformed by Zume}
	 */
	public static double mouseSensitivityHook(double mouseSensitivity) {
		return Zume.transformMouseSensitivity(mouseSensitivity);
	}
	
	/**
	 * The return value of this method can be safely used without checking whether Zume is active.
	 *
	 * @param cinematicCameraEnabled The unmodified cinematic camera state
	 * {@return The new cinematic camera state, transformed by Zume}
	 */
	public static boolean cinematicCameraEnabledHook(boolean cinematicCameraEnabled) {
		return Zume.transformCinematicCamera(cinematicCameraEnabled);
	}
	
	/**
	 * The return value of this method can be safely used without checking whether Zume is active.
	 *
	 * @param scrollDelta The scroll delta (magnitude will be ignored, only the sign is used)
	 * {@return `true` if the invoker should prevent further handling of this scroll event}
	 */
	public static boolean mouseScrollHook(int scrollDelta) {
		return Zume.interceptScroll(scrollDelta);
	}
	//endregion
	
}
