package dev.nolij.zume.api.platform.v1;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;

@ApiStatus.OverrideOnly
public interface IZumeImplementation {
	
	@Contract(pure = true)
	boolean isZoomPressed();
	
	@Contract(pure = true)
	boolean isZoomInPressed();
	
	@Contract(pure = true)
	boolean isZoomOutPressed();
	
	@Contract(pure = true)
	@NotNull
	CameraPerspective getCameraPerspective();
	
	@NonBlocking
	default void onZoomActivate() {
	}
	
}
