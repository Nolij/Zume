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
		\nEnable Cinematic Camera while zooming
		DEFAULT: `true`""")
	public boolean enableCinematicZoom = true;
	
	@Comment("""
		\nMouse Sensitivity will be multiplied by this value while zooming (use `1.0` for no effect)
		NOTE: Only applies if `enableCinematicZoom` is set to `false`
		DEFAULT: `0.3`""")
	public double mouseSensitivityMultiplier = 0.3D;
	
	@Comment("""
		\nPercentage added or subtracted by Zoom In/Out key binds
		DEFAULT: `0.05`""")
	public double zoomIncrement = 0.05D;
	
	@Comment("""
		\nDefault starting zoom percentage
		DEFAULT: `0.5`""")
	public double defaultZoom = 0.5D;
	
	@Comment("""
		\nReset zoom level when Zoom key bind is pressed
		DEFAULT: `true`""")
	public boolean resetOnPress = true;
	
	@Comment("""
		\nMaximum zoom FOV
		DEFAULT: `60.0`""")
	public double maxFOV = 60D;
	
	@Comment("""
		\nMinimum zoom FOV
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
