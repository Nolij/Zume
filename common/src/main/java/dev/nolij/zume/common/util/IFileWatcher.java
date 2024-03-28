package dev.nolij.zume.common.util;

public interface IFileWatcher {
	
	void lock() throws InterruptedException;
	boolean tryLock();
	void unlock();
	
}
