package dev.nolij.zume.impl.config;

import dev.nolij.zson.Zson;
import dev.nolij.zson.ZsonField;
import dev.nolij.zume.impl.Zume;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class ZumeConfigImpl {
	
	@ZsonField(comment = """
		Enable Cinematic Camera while zooming.
		If you disable this, you should also try setting `zoomSmoothnessMs` to `0`.
		DEFAULT: `true`""")
	public boolean enableCinematicZoom = true;
	
	@ZsonField(comment = """
		Mouse Sensitivity will not be reduced below this amount while zoomed in.
		Set to `1.0` to prevent it from being changed at all (not recommended without `enableCinematicZoom`).
		DEFAULT: `0.4`""")
	public double mouseSensitivityFloor = 0.4D;
	
	@ZsonField(comment = """
		Speed for Zoom In/Out key binds & zoom scrolling (if enabled).
		DEFAULT: `20`""")
	public short zoomSpeed = 20;
	
	@ZsonField(comment = """
		Allows you to zoom in and out by scrolling up and down on your mouse while zoom is active.
		This will prevent you from scrolling through your hotbar while zooming if enabled.
		DEFAULT: `true`""")
	public boolean enableZoomScrolling = true;
	
	@ZsonField(comment = """
		FOV changes will be spread out over this many milliseconds.
		Set to `0` to disable animations.
		DEFAULT: `150`""")
	public short zoomSmoothnessMs = 150;
	
	@ZsonField(comment = """
		The exponent used for easing animations.
		You should probably leave this at the default if you don't understand what it does.
		DEFAULT: `4.0`""")
	public double animationEasingExponent = 4D;
	
	@ZsonField(comment = """
		The exponent used for making differences in FOV more uniform.
		You should probably leave this at the default if you don't understand what it does.
		DEFAULT: `2.0`""")
	public double zoomEasingExponent = 2D;
	
	@ZsonField(comment = """
		Default starting zoom percentage.
		DEFAULT: `0.5`""")
	public double defaultZoom = 0.5D;
	
	@ZsonField(comment = """
		If `true`, the Zoom keybind will act as a toggle in first-person.
		If `false`, Zoom will only be active in first-person while the keybind is held.
		DEFAULT: `false`""")
	public boolean toggleMode = false;
	
	@ZsonField(comment = """
		If `true`, the Zoom keybind will act as a toggle in third-person.
		If `false`, Zoom will only be active in third-person while the keybind is held.
		DEFAULT: `true`""")
	public boolean thirdPersonToggleMode = true;
	
	@ZsonField(comment = """
		Minimum zoom FOV.
		DEFAULT: `1.0`""")
	public double minFOV = 1D;
	
	@ZsonField(comment = """
		Maximum third-person zoom distance (in blocks).
		Set to `0.0` to disable third-person zoom.
		DEFAULT: `15.0`""")
	public double maxThirdPersonZoomDistance = 15D;
	
	@ZsonField(comment = """
		Minimum third-person zoom distance (in blocks).
		Set to `4.0` to mimic vanilla.
		DEFAULT: `0.5`""")
	public double minThirdPersonZoomDistance = 0.5D;
	
	@ZsonField(comment = """
		If `true`, the mod will be disabled (on some platforms, key binds will still show in game options; they won't do anything if this is set to `true`).
		Requires re-launch to take effect.
		DEFAULT: `false`""")
	public boolean disable = false;
	
	private static final int EXPECTED_VERSION = 1;
	
	@ZsonField(comment = "Used internally. Don't modify this.")
	public int configVersion = EXPECTED_VERSION;
	
	private static final int MAX_RETRIES = 5;
	private static final Zson ZSON = new Zson();
	
	private static ZumeConfigImpl readFromFile(final File configFile) {
		if (configFile == null || !configFile.exists())
			return null;
		
		int i = 0;
		while (true) {
			try {
				//noinspection DataFlowIssue
				return Zson.map2Obj(Zson.parse(new FileReader(configFile)), ZumeConfigImpl.class);
            } catch (IllegalArgumentException e) {
				if (++i < MAX_RETRIES) {
                    try {
	                    //noinspection BusyWait
	                    Thread.sleep(i * 200L);
						continue;
                    } catch (InterruptedException ignored) {
                        return null;
                    }
                }
				Zume.LOGGER.error("Error parsing config after {} retries: ", i, e);
				return null;
			} catch (IOException e) {
				Zume.LOGGER.error("Error reading config: ", e);
				return null;
            }
        }
	}
	
	private static ZumeConfigImpl readConfigFile() {
		ZumeConfigImpl result = readFromFile(getConfigFile());
		
		if (result == null)
			result = new ZumeConfigImpl();
		
		return result;
	}
	
	private void writeToFile(final File configFile) {
		this.configVersion = EXPECTED_VERSION;
		try (final FileWriter configWriter = new FileWriter(configFile)) {
			ZSON.write(Zson.obj2Map(this), configWriter);
			configWriter.flush();
		} catch (IOException e) {
			throw new RuntimeException("Failed to write config file", e);
		}
	}
	
	private static Consumer<ZumeConfigImpl> consumer;
	private static IFileWatcher instanceWatcher;
	private static IFileWatcher globalWatcher;
	private static File instanceFile = null;
	private static File globalFile = null;
	
	public static void replace(final ZumeConfigImpl newConfig) throws InterruptedException {
		try {
			instanceWatcher.lock();
			try {
				globalWatcher.lock();
				
				newConfig.writeToFile(getConfigFile());
				consumer.accept(newConfig);
			} finally {
				globalWatcher.unlock();
			}
		} finally {
			instanceWatcher.unlock();
		}
	}
	
	
	private static final String CONFIG_PATH_OVERRIDE = System.getProperty("zume.configPathOverride");
	private static final Path GLOBAL_CONFIG_PATH;
	
	static {
		final Path dotMinecraft = switch (Zume.HOST_PLATFORM) {
			case LINUX, UNKNOWN -> Paths.get(System.getProperty("user.home"), ".minecraft");
			case WINDOWS -> Paths.get(System.getenv("APPDATA"), ".minecraft");
			case MAC_OS -> Paths.get(System.getProperty("user.home"), "Library", "Application Support", "minecraft");
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
		
		final ZumeConfigImpl newConfig = readConfigFile();
		
		consumer.accept(newConfig);
	}
	
	public static void init(final Path instanceConfigPath, final String fileName, final Consumer<ZumeConfigImpl> configConsumer) {
		if (consumer != null)
			throw new AssertionError("Config already initialized!");
		
		consumer = configConsumer;
		if (CONFIG_PATH_OVERRIDE == null) {
			instanceFile = instanceConfigPath.resolve(fileName).toFile();
			globalFile = GLOBAL_CONFIG_PATH.resolve(fileName).toFile();
		}
		
		ZumeConfigImpl config = readConfigFile();
		
		// write new options and comment updates to disk
		config.writeToFile(getConfigFile());
		
		consumer.accept(config);
		
		try {
			final IFileWatcher nullWatcher = new NullFileWatcher();
			
			if (config.disable) {
				instanceWatcher = nullWatcher;
				globalWatcher = nullWatcher;
			} else if (CONFIG_PATH_OVERRIDE == null) {
				instanceWatcher = FileWatcher.onFileChange(instanceFile.toPath(), ZumeConfigImpl::reloadConfig);
				globalWatcher = FileWatcher.onFileChange(globalFile.toPath(), ZumeConfigImpl::reloadConfig);
			} else {
				instanceWatcher = nullWatcher;
				globalWatcher = FileWatcher.onFileChange(getConfigFile().toPath(), ZumeConfigImpl::reloadConfig);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to create file watcher", e);
		}
	}
}
