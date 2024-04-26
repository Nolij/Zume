package dev.nolij.zume.api.platform.v0;

public interface IZumeImplementation {
	
	boolean isZoomPressed();
	boolean isZoomInPressed();
	boolean isZoomOutPressed();
	
	CameraPerspective getCameraPerspective();
	
	default void onZoomActivate() {}
	
}
