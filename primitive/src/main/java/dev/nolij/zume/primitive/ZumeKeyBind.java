package dev.nolij.zume.primitive;

import net.minecraft.client.option.KeyBinding;
import org.lwjgl.input.Keyboard;

public enum ZumeKeyBind {
	
	ZOOM("key.zume.zoom", Keyboard.KEY_Z),
	ZOOM_IN("key.zume.zoom_in", Keyboard.KEY_EQUALS),
	ZOOM_OUT("key.zume.zoom_out", Keyboard.KEY_MINUS),
	
	;
	
	public final KeyBinding value;
	
	public boolean isPressed() {
		return Keyboard.isKeyDown(value.code);
	}
	
	ZumeKeyBind(String translationKey, int code) {
		this.value = new KeyBinding(translationKey, code);
	}
	
}
