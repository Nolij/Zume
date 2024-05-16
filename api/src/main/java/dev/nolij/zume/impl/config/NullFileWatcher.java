package dev.nolij.zume.impl.config;

public final class NullFileWatcher implements IFileWatcher {
	@Override
	public void lock() {
	}
	
	@Override
	public boolean tryLock() {
		return true;
	}
	
	@Override
	public void unlock() {
	}
}
