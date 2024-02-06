package dev.nolij.zume.common.config;

import blue.endless.jankson.Comment;
import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.annotation.NonnullByDefault;
import blue.endless.jankson.api.SyntaxError;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.common.util.FileWatcher;

import java.io.*;
import java.util.ConcurrentModificationException;

@NonnullByDefault
public class ZumeConfig {
	
	@Comment("""
		\nEnable Cinematic Camera while zooming.
		If you disable this, you should also try setting `zoomSmoothnessMs` to `0`.
		DEFAULT: `true`""")
	public boolean enableCinematicZoom = true;
	
	@Comment("""
		\nMouse Sensitivity will not be reduced below this amount while zoomed in.
		Set to `1.0` to prevent it from being changed at all (not recommended without `enableCinematicZoom`).
		DEFAULT: `0.4`""")
	public double mouseSensitivityFloor = 0.4D;
	
	@Comment("""
		\nSpeed for Zoom In/Out key binds & zoom scrolling (if enabled).
		DEFAULT: `20`""")
	public short zoomSpeed = 20;
	
	@Comment("""
		\nAllows you to zoom in and out by scrolling up and down on your mouse while zoom is active.
		This will prevent you from scrolling through your hotbar while zooming if enabled.
		DEFAULT: `true`""")
	public boolean enableZoomScrolling = true;
	
	@Comment("""
		\nFOV changes will be spread out over this many milliseconds.
		Set to `0` to disable animations.
		DEFAULT: `150`""")
	public short zoomSmoothnessMs = 150;
	
	@Comment("""
		\nSmoothing animation progress will be raised to this exponent for easing. Higher numbers will feel faster.
		It is recommended to also increase `zoomSmoothnessMs` when increasing this.
		Set to `1` to disable.
		DEFAULT: `4`""")
	public short easingExponent = 4;
	
	@Comment("""
		\nZoom percentage will be squared before being applied if `true`.
		Makes differences in FOV more uniform. You should probably keep this on if you don't understand what it does.
		DEFAULT: `true`""")
	public boolean useQuadratic = true;
	
	@Comment("""
		\nDefault starting zoom percentage.
		DEFAULT: `0.5`""")
	public double defaultZoom = 0.5D;
	
	@Comment("""
		\nIf `true`, the Zoom keybind will act as a toggle. If `false`, Zoom will only be active while the keybind is held.
		DEFAULT: `false`""")
	public boolean toggleMode = false;
	
	@Comment("""
		\nMaximum zoom FOV.
		DEFAULT: `60.0`""")
	public double maxFOV = 60D;
	
	@Comment("""
		\nMinimum zoom FOV.
		DEFAULT: `1.0`""")
	public double minFOV = 1D;
	
	
	@FunctionalInterface
	public interface ConfigConsumer {
		void invoke(ZumeConfig config);
	}
	
	private static final int MAX_RETRIES = 5;
	private static final JsonGrammar JSON_GRAMMAR = JsonGrammar.JANKSON;
	private static final Jankson JANKSON = Jankson.builder()
		.allowBareRootObject()
		.build();
	
	private static ZumeConfig readFromFile(final File configFile) {
		if (!configFile.exists())
			return new ZumeConfig();
		
		int i = 0;
		while (true) {
			try {
				return JANKSON.fromJson(JANKSON.load(configFile), ZumeConfig.class);
            } catch (SyntaxError e) {
				if (i++ < MAX_RETRIES) {
                    try {
	                    //noinspection BusyWait
	                    Thread.sleep(i * 200L);
						continue;
                    } catch (InterruptedException ignored) {
                        return new ZumeConfig();
                    }
                }
				Zume.LOGGER.error("Error parsing config after " + i + " retries: ", e);
				return new ZumeConfig();
			} catch (IOException e) {
				Zume.LOGGER.error("Error reading config: ", e);
				return new ZumeConfig();
            }
        }
	}
	
	private void writeToFile(final File configFile) {
		try (final FileWriter configWriter = new FileWriter(configFile)) {
			JANKSON.toJson(this).toJson(configWriter, JSON_GRAMMAR, 0);
			configWriter.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static ConfigConsumer consumer;
	private static FileWatcher watcher;
	private static File file;
	
	public void modify(ConfigConsumer modifier) throws InterruptedException {
		try {
			watcher.lock();
			modifier.invoke(this);
			this.writeToFile(file);
			consumer.invoke(this);
		} finally {
			watcher.unlock();
		}
    }
	
	public static void init(final File configFile, final ConfigConsumer configConsumer) {
		if (consumer != null)
			throw new AssertionError("Config already initialized!");
		
		consumer = configConsumer;
		file = configFile;
		
		ZumeConfig config = readFromFile(file);
		
		// write new options and comment updates to disk
		config.writeToFile(file);
		
		consumer.invoke(config);
		
		try {
			watcher = FileWatcher.onFileChange(file.toPath(), () -> {				
				Zume.LOGGER.info("Reloading config...");
				
				final ZumeConfig newConfig = readFromFile(file);
				
				consumer.invoke(newConfig);
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
