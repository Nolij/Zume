package dev.nolij.zume;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.util.math.MathHelper;

public class Zume implements ClientModInitializer {
	
	private static final double maxFOV = 60D;
	private static final double minFOV = 1D;
	private static final double zoomDelta = 0.05D;
	public static final double defaultZoom = 0.5D;
	public static double zoom = defaultZoom;
	
	public static double getFOV() {
		return minFOV + ((maxFOV - minFOV) * zoom);
	}
	
	@Override
	public void onInitializeClient() {
		for (final ZumeKeyBind keyBind : ZumeKeyBind.values()) {
			KeyBindingHelper.registerKeyBinding(keyBind.value);
		}
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (ZumeKeyBind.ZOOM_IN.isPressed())
				zoom -= zoomDelta;
			
			if (ZumeKeyBind.ZOOM_OUT.isPressed())
				zoom += zoomDelta;
			
			zoom = MathHelper.clamp(zoom, 0D, 1D);
		});
	}
	
}
