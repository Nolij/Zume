package dev.nolij.zume.common.easing;

public class EasedDouble {
	
	public static final double PLACEHOLDER = Double.NaN;
	
	private short duration;
	private double inverseDuration;
	
	private EasingMethod easingMethod;
	
	private double fromValue = PLACEHOLDER;
	private double targetValue;
	
	private long startTimestamp = 0L;
	private long endTimestamp = 0L;
	
	
	public EasedDouble() {
		this(PLACEHOLDER);
	}
	
	public EasedDouble(final double value) {
		this.targetValue = value;
	}
	
	public void update(final short duration, final EasingMethod easingMethod) {
		this.duration = duration;
		this.inverseDuration = 1D / duration;
		this.easingMethod = easingMethod;
	}
	
	public double getEased() {
		if (isEasing()) {
			final long delta = System.currentTimeMillis() - startTimestamp;
			
			return easingMethod.easeOut(fromValue, targetValue, delta * inverseDuration);
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
		this.fromValue = PLACEHOLDER;
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
		set(getEased(), target);
	}
	
	public boolean isEasing() {
		return duration != 0 && endTimestamp != 0L && System.currentTimeMillis() < endTimestamp;
	}
	
}
