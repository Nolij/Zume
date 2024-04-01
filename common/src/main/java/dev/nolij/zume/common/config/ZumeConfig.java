package dev.nolij.zume.common.config;

import blue.endless.jankson.Comment;
import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.api.SyntaxError;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.common.easing.EasingMethod;
import dev.nolij.zume.common.util.FileWatcher;
import dev.nolij.zume.common.util.IFileWatcher;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

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
		OPTIONS: `SINE`, `LINEAR`, `QUADRATIC`, `CUBIC`, `QUARTIC`, `QUINTIC`, `CIRCULAR`, `EXPONENTIAL`
		DEFAULT: `QUART`""")
	public EasingMethod animationEasingMethod = EasingMethod.QUARTIC;
	
	@Comment("""
		\nThe algorithm responsible for making differences in FOV more uniform.
		You should probably leave this at the default if you don't understand what it does.
		OPTIONS: `SINE`, `LINEAR`, `QUADRATIC`, `CUBIC`, `QUARTIC`, `QUINTIC`, `CIRCULAR`, `EXPONENTIAL`
		DEFAULT: `QUADRATIC`""")
	public EasingMethod zoomEasingMethod = EasingMethod.QUADRATIC;
	
	@Comment("""
		\nDefault starting zoom percentage.
		DEFAULT: `0.5`""")
	public double defaultZoom = 0.5D;
	
	@Comment("""
		\nIf `true`, the Zoom keybind will act as a toggle in first-person. If `false`, Zoom will only be active in first-person while the keybind is held.
		DEFAULT: `false`""")
	public boolean toggleMode = false;
	
	@Comment("""
		\nIf `true`, the Zoom keybind will act as a toggle in third-person. If `false`, Zoom will only be active in third-person while the keybind is held.
		DEFAULT: `false`""")
	public boolean thirdPersonToggleMode = true;
	
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
	
	private static final int EXPECTED_VERSION = 0;
	@Comment("Used internally. Don't modify this.")
	public int configVersion = EXPECTED_VERSION;
	
	
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
		if (configFile == null || !configFile.exists())
			return null;
		
		int i = 0;
		while (true) {
			try {
				return JANKSON.fromJson(JANKSON.load(configFile), ZumeConfig.class);
            } catch (SyntaxError e) {
				if (++i < MAX_RETRIES) {
                    try {
	                    //noinspection BusyWait
	                    Thread.sleep(i * 200L);
						continue;
                    } catch (InterruptedException ignored) {
                        return null;
                    }
                }
				Zume.LOGGER.error("Error parsing config after " + i + " retries: ", e);
				return null;
			} catch (IOException e) {
				Zume.LOGGER.error("Error reading config: ", e);
				return null;
            }
        }
	}
	
	private static ZumeConfig readConfigFile() {
		ZumeConfig result = readFromFile(getConfigFile());
		
		if (result == null)
			result = new ZumeConfig();
		
		return result;
	}
	
	private void writeToFile(final File configFile) {
		this.configVersion = EXPECTED_VERSION;
		try (final FileWriter configWriter = new FileWriter(configFile)) {
			JANKSON.toJson(this).toJson(configWriter, JSON_GRAMMAR, 0);
			configWriter.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static ConfigConsumer consumer;
	private static IFileWatcher instanceWatcher;
	private static IFileWatcher globalWatcher;
	private static File instanceFile = null;
	private static File globalFile = null;
	
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
			instanceWatcher.lock();
			try {
				globalWatcher.lock();
				
				newConfig.writeToFile(getConfigFile());
				consumer.invoke(newConfig);
			} finally {
				globalWatcher.unlock();
			}
		} finally {
			instanceWatcher.unlock();
		}
	}
	
	public void modify(ConfigConsumer modifier) throws InterruptedException {
		final ZumeConfig newConfig = this.clone();
		modifier.invoke(newConfig);
		replace(newConfig);
    }
	
	
	private static final String CONFIG_PATH_OVERRIDE = System.getProperty("zume.configPathOverride");
	private static final Path GLOBAL_CONFIG_PATH;
	
	static {
		final Path dotMinecraft = switch (Zume.HOST_PLATFORM) {
			case LINUX, UNKNOWN -> FileSystems.getDefault().getPath(System.getProperty("user.home"), ".minecraft");
			case WINDOWS -> FileSystems.getDefault().getPath(System.getenv("APPDATA"), ".minecraft");
			case MAC_OS -> FileSystems.getDefault().getPath(System.getProperty("user.home"), "Library", "Application Support", "minecraft");
		};
		
		GLOBAL_CONFIG_PATH = dotMinecraft.resolve("global");
		if (Files.notExists(GLOBAL_CONFIG_PATH)) {
            try {
                Files.createDirectories(GLOBAL_CONFIG_PATH);
            } catch (IOException e) {
                Zume.LOGGER.error("Failed to create global config path: ", e);
            }
        }
	}
	
	public static File getConfigFile() {
		if (CONFIG_PATH_OVERRIDE != null) {
			return new File(CONFIG_PATH_OVERRIDE);
		}
		
		if (instanceFile != null && instanceFile.exists()) {
			return instanceFile;
		}
		
		return globalFile;
	}
	
	public static void reloadConfig() {
		Zume.LOGGER.info("Reloading config...");
		
		final ZumeConfig newConfig = readConfigFile();
		
		consumer.invoke(newConfig);
	}
	
	public static void init(final Path instanceConfigPath, final String fileName, final ConfigConsumer configConsumer) {
		if (consumer != null)
			throw new AssertionError("Config already initialized!");
		
		consumer = configConsumer;
		if (CONFIG_PATH_OVERRIDE == null) {
			instanceFile = instanceConfigPath.resolve(fileName).toFile();
			globalFile = GLOBAL_CONFIG_PATH.resolve(fileName).toFile();
		}
		
		ZumeConfig config = readConfigFile();
		
		// write new options and comment updates to disk
		config.writeToFile(getConfigFile());
		
		consumer.invoke(config);
		
		try {
			final IFileWatcher nullWatcher = new IFileWatcher() {
				@Override
				public void lock() throws InterruptedException {}
				
				@Override
				public boolean tryLock() {
					return true;
				}
				
				@Override
				public void unlock() {}
			};
			
			if (config.disable) {
				instanceWatcher = nullWatcher;
				globalWatcher = nullWatcher;
			} else if (CONFIG_PATH_OVERRIDE == null) {
				instanceWatcher = FileWatcher.onFileChange(instanceFile.toPath(), ZumeConfig::reloadConfig);
				globalWatcher = FileWatcher.onFileChange(globalFile.toPath(), ZumeConfig::reloadConfig);
			} else {
				instanceWatcher = nullWatcher;
				globalWatcher = FileWatcher.onFileChange(getConfigFile().toPath(), ZumeConfig::reloadConfig);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
