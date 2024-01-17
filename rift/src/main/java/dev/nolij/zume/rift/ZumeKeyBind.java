package dev.nolij.zume.rift;

import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.glfw.GLFW;

public enum ZumeKeyBind {
	
	ZOOM("key.zume.zoom", GLFW.GLFW_KEY_Z),
	ZOOM_IN("key.zume.zoom_in", GLFW.GLFW_KEY_EQUAL),
	ZOOM_OUT("key.zume.zoom_out", GLFW.GLFW_KEY_MINUS),
	
	;
	
	public final KeyBinding value;
	
	public boolean isPressed() {
		return value.isKeyDown();
	}
	
	ZumeKeyBind(String translationKey, int code, String category) {
		this.value = new KeyBinding(translationKey, code, category);
	}
	
	ZumeKeyBind(String translationKey, int code) {
		this(translationKey, code, "category.zume");
	}
	
}