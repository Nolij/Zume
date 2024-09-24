package dev.nolij.zume.primitive;

import net.minecraft.client.KeyMapping;
import org.lwjgl.input.Keyboard;

public enum ZumeKeyBind {
	
	ZOOM("zume.zoom", Keyboard.KEY_Z),
	ZOOM_IN("zume.zoom_in", Keyboard.KEY_EQUALS),
	ZOOM_OUT("zume.zoom_out", Keyboard.KEY_MINUS),
	
	;
	
	public final KeyMapping value;
	
	public boolean isPressed() {
		return Keyboard.isKeyDown(value.key);
	}
	
	ZumeKeyBind(String translationKey, int keyId) {
		this.value = new KeyMapping(translationKey, keyId);
	}
	
}
