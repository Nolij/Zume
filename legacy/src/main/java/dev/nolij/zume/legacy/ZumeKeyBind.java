package dev.nolij.zume.legacy;

import net.minecraft.client.option.KeyBinding;
import org.lwjgl.input.Keyboard;

public enum ZumeKeyBind {
	
	ZOOM("zume.zoom", Keyboard.KEY_Z),
	ZOOM_IN("zume.zoom_in", Keyboard.KEY_EQUALS),
	ZOOM_OUT("zume.zoom_out", Keyboard.KEY_MINUS),
	
	;
	
	public final KeyBinding value;
	
	public boolean isPressed() {
		return value.isPressed();
	}
	
	ZumeKeyBind(String translationKey, int code) {
		this.value = LegacyZume.newKeyBinding(translationKey, code, "zume");
	}
	
}