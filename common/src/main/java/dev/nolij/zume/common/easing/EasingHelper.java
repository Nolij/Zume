package dev.nolij.zume.common.easing;

public final class EasingHelper {
	
	public static double applyOut(double progress, double exponent) {
		return Math.pow(progress, exponent);
	}
	
	public static double inverseOut(double progress, double exponent) {
		return Math.pow(progress, 1D / exponent);
	}
	
	public static double applyIn(double progress, double exponent) {
		return 1 - Math.pow(1 - progress, exponent);
	}
	
	public static double inverseIn(double progress, double exponent) {
		return 1 - Math.pow(1 - progress, 1D / exponent);
	}
	
	public static double easeOut(double start, double end, double progress, double exponent) {
		return start + ((end - start) * applyOut(progress, exponent));
	}
	
	public static double easeIn(double start, double end, double progress, double exponent) {
		return start + ((end - start) * applyIn(progress, exponent));
	}
	
	public static double easeInOut(double start, double end, double progress, double exponent) {
		return easeIn(start, end, easeIn(applyIn(progress, exponent), applyOut(progress, exponent), progress, 1), 1);
	}
	
	public static double inverseEaseOut(double start, double end, double value, double exponent) {
		return Math.pow((value - start) / (end - start), 1D / exponent);
	}
	
	public static double inverseEaseIn(double start, double end, double value, double exponent) {
		return 1 - Math.pow(1 - ((value - start) / (end - start)), 1D / exponent);
	}
	
//	public static double inverseEaseInOut(double start, double end, double value, double exponent) {
//		
//	}
	
}
