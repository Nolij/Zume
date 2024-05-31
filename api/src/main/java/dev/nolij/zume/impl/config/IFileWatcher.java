package dev.nolij.zume.impl.config;

public interface IFileWatcher {
	
	void lock() throws InterruptedException;
	boolean tryLock();
	void unlock();
	
}
