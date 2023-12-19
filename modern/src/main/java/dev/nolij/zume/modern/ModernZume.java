package dev.nolij.zume.modern;

import dev.nolij.zume.common.IZumeProvider;
import dev.nolij.zume.common.Zume;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;

public class ModernZume implements ClientModInitializer, IZumeProvider {
	
	public static ModernZume INSTANCE;
	
	@Override
	public void onInitializeClient() {
		INSTANCE = this;
		
		for (final ZumeKeyBind keyBind : ZumeKeyBind.values()) {
			KeyBindingHelper.registerKeyBinding(keyBind.value);
		}
		
		Zume.init(this, FabricLoader.getInstance().getConfigDir().resolve(Zume.CONFIG_FILE_NAME).toFile());
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
