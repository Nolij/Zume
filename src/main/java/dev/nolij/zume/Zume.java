package dev.nolij.zume;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Zume implements ClientModInitializer {
	
	public static final String MOD_ID = "zume";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	private static final String CONFIG_FILE = MOD_ID + ".json5";
	
	public static ZumeConfig CONFIG;
	
	public static double zoom = -1D;
	
	public static double getFOV() {
		if (zoom == -1)
			zoom = CONFIG.defaultZoom;
		
		return CONFIG.minFOV + ((CONFIG.maxFOV - CONFIG.minFOV) * zoom);
	}
	
	@Override
	public void onInitializeClient() {
		CONFIG = ZumeConfig.fromFile(FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE).toFile());
		
		for (final ZumeKeyBind keyBind : ZumeKeyBind.values()) {
			KeyBindingHelper.registerKeyBinding(keyBind.value);
		}
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!ZumeKeyBind.ZOOM.isPressed())
				return;
				
			if (ZumeKeyBind.ZOOM_IN.wasPressed() || ZumeKeyBind.ZOOM_IN.isPressed())
				zoom -= CONFIG.zoomIncrement;
			
			if (ZumeKeyBind.ZOOM_OUT.wasPressed() || ZumeKeyBind.ZOOM_OUT.isPressed())
				zoom += CONFIG.zoomIncrement;
			
			zoom = MathHelper.clamp(zoom, 0D, 1D);
		});
	}
	
}
