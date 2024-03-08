package dev.nolij.zume.common.util;

public class LerpedDouble {
	
	public static final double PLACEHOLDER = Double.NaN;
	
	private short duration;
	private double inverseDuration;
	
	private short easingExponent;
	
	private double fromValue = PLACEHOLDER;
	private double targetValue;
	
	private long startTimestamp = 0L;
	private long endTimestamp = 0L;
	
	
	public LerpedDouble() {
		this(PLACEHOLDER);
	}
	
	public LerpedDouble(final double value) {
		this.targetValue = value;
	}
	
	private double easeProgress(final double inversedProgress) {
		var easedProgress = inversedProgress;
		
		for (int i = 0; i < easingExponent; i++) {
			easedProgress *= inversedProgress;
		}
		
		return 1 - easedProgress;
	}
	
	private double lerp(final double start, final double end, final double progress) {
		return start + ((end - start) * easeProgress(1 - progress * inverseDuration));
	}
	
	public void update(final short duration, final short easingExponent) {
		this.duration = duration;
		this.inverseDuration = 1D / duration;
		this.easingExponent = easingExponent;
	}
	
	public double getLerped() {
		if (isLerping()) {
			final long delta = System.currentTimeMillis() - startTimestamp;
			
			return lerp(fromValue, targetValue, delta);
		}
		
		return targetValue;
	}
	
	public void fillPlaceholder(double value) {
		if (Double.isNaN(fromValue))
			fromValue = value;
		if (Double.isNaN(targetValue))
			targetValue = value;
	}
	
	public double getTarget() {
		return targetValue;
	}
	
	public void setInstant(double target) {
		this.startTimestamp = 0L;
		this.endTimestamp = 0L;
		this.fromValue = -1D;
		this.targetValue = target;
	}
	
	public void set(double from, double target) {
		if (duration == 0) {
			setInstant(target);
			return;
		}
		
		this.startTimestamp = System.currentTimeMillis();
		this.endTimestamp = startTimestamp + duration;
		this.fromValue = from;
		this.targetValue = target;
	}
	
	public void set(double target) {		
		set(getLerped(), target);
	}
	
	public boolean isLerping() {
		return duration != 0 && endTimestamp != 0L && System.currentTimeMillis() < endTimestamp;
	}
	
}
