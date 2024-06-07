// Based off of https://gist.githubusercontent.com/danielflower/f54c2fe42d32356301c68860a4ab21ed/raw/d09c312b4e40b17cdce310992da89dc06aabb98a/FileWatcher.java
// Original License (all modifications are still distributed under this project's license): https://gist.github.com/danielflower/f54c2fe42d32356301c68860a4ab21ed?permalink_comment_id=2352260#gistcomment-2352260

package dev.nolij.zume.impl.config;

import dev.nolij.zume.impl.Zume;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.Semaphore;

public class FileWatcher implements IFileWatcher {
	
	private static final long DEBOUNCE_DURATION_MS = 500L;
	
	/**
	 * Starts watching a file and the given path and calls the callback when it is changed.
	 * A shutdown hook is registered to stop watching. To control this yourself, create an
	 * instance and use the start/stop methods.
	 */
	public static FileWatcher onFileChange(Path file, Runnable callback) throws IOException {
		final FileWatcher watcher = new FileWatcher();
		watcher.start(file, callback);
		
		return watcher;
	}
	
	private WatchService watchService;
	private Thread thread;
	private long debounce = 0L;
	private final Semaphore semaphore = new Semaphore(1);
	
	@Override
	public void lock() throws InterruptedException {
		synchronized (semaphore) {
			semaphore.acquire();
		}
	}
	
	@Override
	public boolean tryLock() {
		synchronized (semaphore) {
			return semaphore.tryAcquire();
		}
	}
	
	@Override
	public void unlock() {
		synchronized (semaphore) {
			if (semaphore.availablePermits() > 0)
				return;
			
			semaphore.release();
		}
	}
	
	public void start(Path file, Runnable callback) throws IOException {
		watchService = FileSystems.getDefault().newWatchService();
		Path parent = file.getParent();
		parent.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
		
		thread = new Thread(() -> {
			while (true) {
				WatchKey wk = null;
				try {
					wk = watchService.take();
					for (final WatchEvent<?> event : wk.pollEvents()) {
						final Path changed = parent.resolve((Path) event.context());
						boolean locked = false;
						try {
							if ((locked = tryLock()) &&
								System.currentTimeMillis() > debounce &&
								Files.exists(changed) && 
								Files.isSameFile(changed, file)) {
								callback.run();
								debounce = System.currentTimeMillis() + DEBOUNCE_DURATION_MS;
								break;
							}
						} catch (NoSuchFileException ignored) {
						} catch (IOException e) {
							Zume.LOGGER.error("Error in config watcher: ", e);
						} finally {
							if (locked)
								unlock();
						}
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				} finally {
					if (wk != null) {
						wk.reset();
					}
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
	
	public void stop() {
		thread.interrupt();
		watchService.close();
	}
	
}
