package dev.nolij.zume.impl.config;

import dev.nolij.zson.Zson;
import dev.nolij.zson.ZsonParser;
import dev.nolij.zson.ZsonValue;
import dev.nolij.zson.ZsonWriter;
import dev.nolij.zume.impl.Zume;

import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

public class ZumeConfigImpl {
	
	@Comment("""
		Enable Cinematic Camera while zooming.
		If you disable this, you should also try setting `zoomSmoothnessMs` to `0`.
		DEFAULT: `true`""")
	public boolean enableCinematicZoom = true;
	
	@Comment("""
		Mouse Sensitivity will not be reduced below this amount while zoomed in.
		Set to `1.0` to prevent it from being changed at all (not recommended without `enableCinematicZoom`).
		DEFAULT: `0.4`""")
	public double mouseSensitivityFloor = 0.4D;
	
	@Comment("""
		Speed for Zoom In/Out key binds & zoom scrolling (if enabled).
		DEFAULT: `20`""")
	public short zoomSpeed = 20;
	
	@Comment("""
		Allows you to zoom in and out by scrolling up and down on your mouse while zoom is active.
		This will prevent you from scrolling through your hotbar while zooming if enabled.
		DEFAULT: `true`""")
	public boolean enableZoomScrolling = true;
	
	@Comment("""
		FOV changes will be spread out over this many milliseconds.
		Set to `0` to disable animations.
		DEFAULT: `150`""")
	public short zoomSmoothnessMs = 150;
	
	@Comment("""
		The exponent used for easing animations.
		You should probably leave this at the default if you don't understand what it does.
		DEFAULT: `4.0`""")
	public double animationEasingExponent = 4D;
	
	@Comment("""
		The exponent used for making differences in FOV more uniform.
		You should probably leave this at the default if you don't understand what it does.
		DEFAULT: `2.0`""")
	public double zoomEasingExponent = 2D;
	
	@Comment("""
		Default starting zoom percentage.
		DEFAULT: `0.5`""")
	public double defaultZoom = 0.5D;
	
	@Comment("""
		If `true`, the Zoom keybind will act as a toggle in first-person.
		If `false`, Zoom will only be active in first-person while the keybind is held.
		DEFAULT: `false`""")
	public boolean toggleMode = false;
	
	@Comment("""
		If `true`, the Zoom keybind will act as a toggle in third-person.
		If `false`, Zoom will only be active in third-person while the keybind is held.
		DEFAULT: `true`""")
	public boolean thirdPersonToggleMode = true;
	
	@Comment("Minimum zoom FOV.\nDEFAULT: `1.0`")
	public double minFOV = 1D;
	
	@Comment("""
		Maximum third-person zoom distance (in blocks).
		Set to `0.0` to disable third-person zoom.
		DEFAULT: `15.0`""")
	public double maxThirdPersonZoomDistance = 15D;
	
	@Comment("""
		Minimum third-person zoom distance (in blocks).
		Set to `4.0` to mimic vanilla.
		DEFAULT: `0.5`""")
	public double minThirdPersonZoomDistance = 0.5D;
	
	@Comment("""
		If `true`, the mod will be disabled (on some platforms, key binds will still show in game options; they won't do anything if this is set to `true`).
		Requires re-launch to take effect.
		DEFAULT: `false`""")
	public boolean disable = false;
	
	private static final int EXPECTED_VERSION = 1;
	
	@Comment("Used internally. Don't modify this.")
	public int configVersion = EXPECTED_VERSION;
	
	private static final int MAX_RETRIES = 5;
	private static final ZsonWriter ZSON = new ZsonWriter();
	
	private static ZumeConfigImpl readFromFile(final File configFile) {
		if (configFile == null || !configFile.exists())
			return null;
		
		int i = 0;
		while (true) {
			try {
				return fromMap(ZsonParser.parse(new FileReader(configFile)));
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
			ZSON.write(this.toMap(), configWriter);
			configWriter.flush();
		} catch (IOException e) {
			throw new RuntimeException("Failed to write config file", e);
		}
	}
	
	private Map<String, ZsonValue> toMap() {
		Map<String, ZsonValue> map = Zson.object();
		for (Field field : ZumeConfigImpl.class.getDeclaredFields()) {
			if(Modifier.isStatic(field.getModifiers())) continue;
			Comment comment = field.getAnnotation(Comment.class);
			if (comment == null) continue;
			try {
				map.put(field.getName(), new ZsonValue(comment.value(), field.get(this)));
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Failed to get field " + field.getName(), e);
			}
		}
		return map;
	}
	
	private static ZumeConfigImpl fromMap(final Map<String, ZsonValue> map) {
		ZumeConfigImpl result = new ZumeConfigImpl();
		for (Field field : ZumeConfigImpl.class.getDeclaredFields()) {
			if(Modifier.isStatic(field.getModifiers())) continue;
			setField(field, result, map.get(field.getName()).value);
		}
		return result;
	}
	
	private static <T> void setField(Field field, ZumeConfigImpl config, Object value) {
		@SuppressWarnings("unchecked")
		Class<T> type = (Class<T>) field.getType();
		try {
			if(type.isPrimitive()) {
				switch (type.getName()) {
					case "boolean" -> field.setBoolean(config, (boolean) value);
					case "short" -> field.setShort(config, (short) (int) value);
					case "int" -> field.setInt(config, (int) value);
					case "float" -> field.setFloat(config, (float) (double) value);
					case "double" -> field.setDouble(config, (double) value);
				}
			} else {
				field.set(config, type.cast(value));
			}
		} catch (Exception e) {
			throw new RuntimeException(
				"Failed to set field " + field.getName() + " (type " + type.getSimpleName() + ") to " + value + " " +
					"(type " + value.getClass().getSimpleName() + ")", e
			);
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
	
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Comment {
		String value();
	}
}
