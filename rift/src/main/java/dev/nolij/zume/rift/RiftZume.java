package dev.nolij.zume.rift;

import dev.nolij.zume.common.IZumeProvider;
import dev.nolij.zume.common.Zume;
import net.minecraft.client.settings.KeyBinding;
import org.dimdev.rift.listener.client.KeyBindingAdder;
import org.dimdev.riftloader.RiftLoader;
import org.dimdev.riftloader.listener.InitializationListener;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class RiftZume implements InitializationListener, KeyBindingAdder, IZumeProvider {
	
	@Override
	public void onInitialization() {
		Zume.LOGGER.info("Loading Rift Zume...");
		
		MixinBootstrap.init();
		Mixins.addConfiguration("zume-rift.mixins.json");
		
		final File configDir = RiftLoader.instance.configDir;
		final Path configPath = configDir.toPath();
		if (!Files.exists(configPath)) {
			try {
				Files.createDirectory(configPath);
			} catch (IOException e) {
				Zume.LOGGER.error("Error initializing config: ", e);
				throw new AssertionError(e);
			}
		}
		
		Zume.init(this, new File(configDir, Zume.CONFIG_FILE_NAME));
	}
	
	@Override
	public Collection<? extends KeyBinding> getKeyBindings() {
		return Arrays.stream(ZumeKeyBind.values()).map(x -> x.value).collect(Collectors.toList());
	}
	
	@Override
	public boolean isZoomPressed() {
		return ZumeKeyBind.ZOOM.isPressed();
	}
	
	@Override
	public boolean isZoomInPressed() {
		return ZumeKeyBind.ZOOM_IN.isPressed();
	}
	
	@Override
	public boolean isZoomOutPressed() {
		return ZumeKeyBind.ZOOM_OUT.isPressed();
	}
	
}
