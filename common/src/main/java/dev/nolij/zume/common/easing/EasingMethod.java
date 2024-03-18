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
	SINE {
		@Override
		public double apply(double value) {
			return 1 - Math.sin(value * Math.PI / 2);
		}
	},
	QUADRATIC {
		@Override
		public double apply(double value) {
			return value * value;
		}
	},
	CUBIC {
		@Override
		public double apply(double value) {
			return Math.pow(value, 3);
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
	CIRCULAR {
		@Override
		public double apply(double value) {
			return 1 - Math.sqrt(1 - value * value);
		}
	},
	EXPONENTIAL {
		@Override
		public double apply(double value) {
			return Math.pow(2, 10 * (value - 1));
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
