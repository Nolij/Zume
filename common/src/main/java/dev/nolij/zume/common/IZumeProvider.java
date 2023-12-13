package dev.nolij.zume.common;

public interface IZumeProvider {
	
	boolean isZoomPressed();
	boolean isZoomInPressed();
	boolean isZoomOutPressed();
	
	default void onZoomActivate() {}
	
}
