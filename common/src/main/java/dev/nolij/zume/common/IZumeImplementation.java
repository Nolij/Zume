package dev.nolij.zume.common;

public interface IZumeImplementation {
	
	boolean isZoomPressed();
	boolean isZoomInPressed();
	boolean isZoomOutPressed();
	
	default void onZoomActivate() {}
	
}
