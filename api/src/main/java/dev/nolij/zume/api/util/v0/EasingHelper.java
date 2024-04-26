package dev.nolij.zume.api.util.v0;

public final class EasingHelper {
	
	public static double linear(double start, double end, double progress) {
		return start + ((end - start) * progress);
	}
	
	public static double inverseLinear(double start, double end, double value) {
		return (value - start) / (end - start);
	}
	
	public static double out(double progress, double exponent) {
		return Math.pow(progress, exponent);
	}
	
	public static double inverseOut(double progress, double exponent) {
		return Math.pow(progress, 1D / exponent);
	}
	
	public static double out(double start, double end, double progress, double exponent) {
		return linear(start, end, out(progress, exponent));
	}
	
	public static double inverseOut(double start, double end, double value, double exponent) {
		return inverseOut(inverseLinear(start, end, value), exponent);
	}
	
	public static double in(double progress, double exponent) {
		return 1 - Math.pow(1 - progress, exponent);
	}
	
	public static double inverseIn(double progress, double exponent) {
		return 1 - Math.pow(1 - progress, 1D / exponent);
	}
	
	public static double in(double start, double end, double progress, double exponent) {
		return linear(start, end, in(progress, exponent));
	}
	
	public static double inverseIn(double start, double end, double value, double exponent) {
		return inverseIn(inverseLinear(start, end, value), exponent);
	}
	
	public static double inOut(double progress, double exponent) {
		return (progress < 0.5 ? out(progress, exponent) : in(progress, exponent)) * Math.pow(2, exponent - 1);
	}
	
	public static double inverseInOut(double progress, double exponent) {
		return progress < 0.5
		       ? Math.pow(progress / Math.pow(2, exponent - 1), 1D / exponent)
		       : -Math.pow((1 - progress) / Math.pow(2, exponent - 1), 1D / exponent) + 1;
	}
	
	public static double inOut(double start, double end, double progress, double exponent) {
		return linear(start, end, inOut(progress, exponent));
	}
	
	public static double inverseInOut(double start, double end, double value, double exponent) {
		return inverseInOut(inverseLinear(start, end, value), exponent);
	}
	
}
