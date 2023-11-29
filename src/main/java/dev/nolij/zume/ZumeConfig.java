package dev.nolij.zume;

import blue.endless.jankson.Comment;
import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.annotation.NonnullByDefault;
import blue.endless.jankson.api.SyntaxError;
import dev.nolij.zume.util.FileWatcher;

import java.io.*;

@NonnullByDefault
public class ZumeConfig {
	
	@Comment("""
		\nEnable Cinematic Camera while zooming.
		If you disable this, you should also try setting `zoomSmoothness` to `0`.
		DEFAULT: `true`""")
	public boolean enableCinematicZoom = true;
	
	@Comment("""
		\nMouse Sensitivity will not be reduced below this amount while zoomed in.
		Set to `1.0` to prevent it from being changed at all (not recommended without `enableCinematicZoom`).
		DEFAULT: `0.4`""")
	public double mouseSensitivityFloor = 0.4D;
	
	@Comment("""
		\nSpeed for Zoom In/Out key binds.
		DEFAULT: `20`""")
	public short zoomSpeed = 20;
	
	@Comment("""
		\nAllows you to zoom in and out by scrolling up and down on your mouse while zoom is active.
		This will prevent you from scrolling through your hotbar while zooming if enabled.
		DEFAULT: `true`""")
	public boolean enableZoomScrolling = true;
	
	@Comment("""
		\nFOV changes will be spread out over this many milliseconds.
		Set to `0` to minimize latency.
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
		\nMaximum zoom FOV.
		DEFAULT: `60.0`""")
	public double maxFOV = 60D;
	
	@Comment("""
		\nMinimum zoom FOV.
		DEFAULT: `1.0`""")
	public double minFOV = 1D;
	
	
	private static final JsonGrammar JSON_GRAMMAR = JsonGrammar.JANKSON;
	private static final Jankson JANKSON = Jankson.builder()
		.allowBareRootObject()
		.build();
	
	@FunctionalInterface
	public interface ConfigSetter {
		void set(ZumeConfig config);
	}
	
	private static ZumeConfig readFromFile(final File CONFIG_FILE) {
		if (!CONFIG_FILE.exists())
			return new ZumeConfig();
		
		try {
			return JANKSON.fromJson(JANKSON.load(CONFIG_FILE), ZumeConfig.class);
		} catch (IOException | SyntaxError e) {
			Zume.LOGGER.error(e.toString());
			return new ZumeConfig();
		}
	}
	
	private void writeToFile(final File CONFIG_FILE) {
		try (final FileWriter configWriter = new FileWriter(CONFIG_FILE)) {
			JANKSON.toJson(this).toJson(configWriter, JSON_GRAMMAR, 0);
			configWriter.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void create(final File CONFIG_FILE, ConfigSetter setter) {		
		ZumeConfig config = readFromFile(CONFIG_FILE);
		
		// write new options and comment updates to disk
		config.writeToFile(CONFIG_FILE);
		
		setter.set(config);
		
		try {
			FileWatcher.onFileChange(CONFIG_FILE.toPath(), () -> {
				Zume.LOGGER.info("Reloading config...");
				
				ZumeConfig newConfig = readFromFile(CONFIG_FILE);
				
				setter.set(newConfig);
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
