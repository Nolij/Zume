// Based off of https://gist.githubusercontent.com/danielflower/f54c2fe42d32356301c68860a4ab21ed/raw/d09c312b4e40b17cdce310992da89dc06aabb98a/FileWatcher.java
// License: https://gist.github.com/danielflower/f54c2fe42d32356301c68860a4ab21ed?permalink_comment_id=2352260#gistcomment-2352260

package dev.nolij.zume.util;

import java.io.IOException;
import java.nio.file.*;

public class FileWatcher {
	
	private static final long DEBOUNCE_DURATION_MS = 500L;
	
	private WatchService watchService;
	private Thread thread;
	private long debounce = 0L;
	
	public interface Callback {
		void invoke();
	}
	
	/**
	 * Starts watching a file and the given path and calls the callback when it is changed.
	 * A shutdown hook is registered to stop watching. To control this yourself, create an
	 * instance and use the start/stop methods.
	 */
	public static void onFileChange(Path file, Callback callback) throws IOException {
		FileWatcher watcher = new FileWatcher();
		watcher.start(file, callback);
		Runtime.getRuntime().addShutdownHook(new Thread(watcher::stop));
	}
	
	public void start(Path file, Callback callback) throws IOException {
		watchService = FileSystems.getDefault().newWatchService();
		Path parent = file.getParent();
		parent.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
		
		thread = new Thread(() -> {
			while (true) {
				WatchKey wk = null;
				try {
					wk = watchService.take();
					for (WatchEvent<?> event : wk.pollEvents()) {
						Path changed = parent.resolve((Path) event.context());
						if (System.currentTimeMillis() > debounce && 
							Files.exists(changed) && Files.isSameFile(changed, file)) {
							debounce = System.currentTimeMillis() + DEBOUNCE_DURATION_MS;
							callback.invoke();
							break;
						}
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					if (wk != null) {
						wk.reset();
					}
				}
			}
		});
		thread.start();
	}
	
	public void stop() {
		thread.interrupt();
		try {
			watchService.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
