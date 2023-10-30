package dev.nolij.zume;

import blue.endless.jankson.Comment;
import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.annotation.NonnullByDefault;
import blue.endless.jankson.api.SyntaxError;

import java.io.*;

@NonnullByDefault
public class ZumeConfig {
	
	@Comment("""
		\nEnable Cinematic Camera while zooming.
		If you disable this, you should also try setting `zoomSmoothness` to `0`.
		DEFAULT: `true`""")
	public boolean enableCinematicZoom = true;
	
	@Comment("""
		\nMouse Sensitivity will be multiplied by this value while zooming (use `1.0` for no effect).
		Only applies if `enableCinematicZoom` is set to `false`.
		DEFAULT: `0.3`""")
	public double mouseSensitivityMultiplier = 0.3D;
	
	@Comment("""
		\nSpeed for Zoom In/Out key binds.
		DEFAULT: `15`""")
	public short zoomSpeed = 15;
	
	@Comment("""
		\nAllows you to zoom in and out by scrolling up and down on your mouse while zoom is active.
		This will prevent you from scrolling through your hotbar while zooming if enabled.
		DEFAULT: `true`""")
	public boolean enableZoomScrolling = true;
	
	@Comment("""
		\nFOV changes will be spread out over this many milliseconds.
		Set to `0` to minimize latency.
		DEFAULT: `75`""")
	public short zoomSmoothness = 75;
	
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
		\nReset zoom level when Zoom key bind is pressed.
		DEFAULT: `true`""")
	public boolean resetOnPress = true;
	
	@Comment("""
		\nMaximum zoom FOV.
		DEFAULT: `60.0`""")
	public double maxFOV = 60D;
	
	@Comment("""
		\nMinimum zoom FOV.
		DEFAULT: `1.0`""")
	public double minFOV = 1D;
	
	
	private static final JsonGrammar JSON_GRAMMAR = JsonGrammar.JANKSON;
	
	public static ZumeConfig fromFile(final File CONFIG_FILE) {
		final Jankson JANKSON = Jankson.builder()
			.allowBareRootObject()
			.build();
		
		ZumeConfig config;
		
		if (CONFIG_FILE.exists()) {
			try {
				config = JANKSON.fromJson(JANKSON.load(CONFIG_FILE), ZumeConfig.class);
			} catch (IOException | SyntaxError e) {
				Zume.LOGGER.error(e.toString());
				config = new ZumeConfig();
			}
		} else {
			config = new ZumeConfig();
		}
		
		try (final FileWriter configWriter = new FileWriter(CONFIG_FILE)) {
			JANKSON.toJson(config).toJson(configWriter, JSON_GRAMMAR, 0);
			configWriter.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return config;
	}
	
}
