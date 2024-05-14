package dev.nolij.zume.api.util.v0;

import org.jetbrains.annotations.Contract;

public final class MathHelper {
	
	@Contract(pure = true)
	public static int sign(final int input) {
		return input >> (Integer.SIZE - 1) | 1;
	}
	
	@Contract(pure = true)
	public static double clamp(final double value, final double min, final double max) {
		return Math.max(Math.min(value, max), min);
	}
	
}
