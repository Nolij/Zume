package dev.nolij.zume.impl;

public interface IZumeImplementation {
	
	boolean isZoomPressed();
	boolean isZoomInPressed();
	boolean isZoomOutPressed();
	
	CameraPerspective getCameraPerspective();
	
	default void onZoomActivate() {}
	
}
