package dev.nolij.zume.common.config;

import blue.endless.jankson.Comment;
import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.annotation.NonnullByDefault;
import blue.endless.jankson.api.SyntaxError;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.common.easing.EasingMethod;
import dev.nolij.zume.common.util.FileWatcher;

import java.io.*;

@NonnullByDefault
public class ZumeConfig implements Cloneable {
	
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
		\nThe algorithm responsible easing animations.
		You should probably leave this at the default if you don't understand what it does.
		OPTIONS: `LINEAR`, `QUADRATIC`, `QUARTIC`, `QUINTIC`
		DEFAULT: `QUART`""")
	public EasingMethod animationEasingMethod = EasingMethod.QUARTIC;
	
	@Comment("""
		\nThe algorithm responsible for making differences in FOV more uniform.
		You should probably leave this at the default if you don't understand what it does.
		OPTIONS: `LINEAR`, `QUADRATIC`, `QUARTIC`, `QUINTIC`
		DEFAULT: `QUADRATIC`""")
	public EasingMethod zoomEasingMethod = EasingMethod.QUADRATIC;
	
	@Comment("""
		\nDefault starting zoom percentage.
		DEFAULT: `0.5`""")
	public double defaultZoom = 0.5D;
	
	@Comment("""
		\nIf `true`, the Zoom keybind will act as a toggle. If `false`, Zoom will only be active while the keybind is held.
		DEFAULT: `false`""")
	public boolean toggleMode = false;
	
	@Comment("""
		\nMinimum zoom FOV.
		DEFAULT: `1.0`""")
	public double minFOV = 1D;
	
	@Comment("""
        \nMaximum third-person zoom distance (in blocks).
        Set to `0.0` to disable third-person zoom.
        DEFAULT: `15.0`""")
	public double maxThirdPersonZoomDistance = 15D;
	
	@Comment("""
        \nMinimum third-person zoom distance (in blocks).
        Set to `0.0` to mimic vanilla.
        DEFAULT: `0.5`""")
	public double minThirdPersonZoomDistance = 0.5D;
	
	@Comment("""
		\nIf `true`, the mod will be disabled (on some platforms, key binds will still show in game options; they won't do anything if this is set to `true`).
		Requires re-launch to take effect.
		DEFAULT: `false`""")
	public boolean disable = false;
	
	
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
	
	@Override
	public ZumeConfig clone() {
		try {
			return (ZumeConfig) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}
	
	public static void replace(final ZumeConfig newConfig) throws InterruptedException {
		try {
			watcher.lock();
			newConfig.writeToFile(file);
			consumer.invoke(newConfig);
		} finally {
			watcher.unlock();
		}
	}
	
	public void modify(ConfigConsumer modifier) throws InterruptedException {
		final ZumeConfig newConfig = this.clone();
		modifier.invoke(newConfig);
		replace(newConfig);
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
			
			if (config.disable)
				watcher.stop();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
