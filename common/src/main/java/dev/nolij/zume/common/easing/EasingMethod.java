package dev.nolij.zume.common.easing;

public enum EasingMethod {
	
	LINEAR {
		@Override
		public double apply(double value) {
			return value;
		}
		
		@Override
		public double easeIn(double start, double end, double progress) {
			return start + ((end - start) * progress);
		}
		
		@Override
		public double easeOut(double start, double end, double progress) {
			return easeIn(start, end, progress);
		}
		
		@Override
		public double easeInOut(double start, double end, double progress) {
			return easeIn(start, end, progress);
		}
	},
	QUADRATIC {
		@Override
		public double apply(double value) {
			return value * value;
		}
	},
	QUARTIC {
		@Override
		public double apply(double value) {
			return Math.pow(value, 4);
		}
	},
	QUINTIC {
		@Override
		public double apply(double value) {
			return Math.pow(value, 5);
		}
	},
	
	;
	
	public abstract double apply(double value);
	
	public double easeIn(double start, double end, double progress) {
		return start + ((end - start) * apply(progress));
	}
	
	public double easeOut(double start, double end, double progress) {
		return start + ((end - start) * (1 - apply(1 - progress)));
	}
	
	public double easeInOut(double start, double end, double progress) {
		return LINEAR.easeIn(start, end, LINEAR.easeIn(apply(progress), 1 - apply(1 - progress), progress));
	}
	
}
