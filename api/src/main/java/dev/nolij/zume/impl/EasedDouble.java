package dev.nolij.zume.impl;

import dev.nolij.zume.api.util.v1.EasingHelper;

public class EasedDouble {
	
	private short duration;
	private double inverseDuration;
	
	private double exponent;
	
	private double fromValue;
	private double targetValue;
	
	private long startTimestamp = 0L;
	private long endTimestamp = 0L;
	
	
	public EasedDouble(final double value) {
		this.targetValue = value;
	}
	
	public void update(final short duration, final double exponent) {
		this.duration = duration;
		this.inverseDuration = 1D / duration;
		this.exponent = exponent;
	}
	
	public double getEased() {
		if (isEasing()) {
			final long delta = System.currentTimeMillis() - startTimestamp;
			
			return EasingHelper.in(fromValue, targetValue, delta * inverseDuration, exponent);
		}
		
		return targetValue;
	}
	
	public double getTarget() {
		return targetValue;
	}
	
	public void setInstant(double target) {
		this.startTimestamp = 0L;
		this.endTimestamp = 0L;
		this.fromValue = 0D;
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
		return System.currentTimeMillis() < endTimestamp;
	}
	
}
