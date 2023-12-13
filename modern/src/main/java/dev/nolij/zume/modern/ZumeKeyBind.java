package dev.nolij.zume.modern;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public enum ZumeKeyBind {
	
	ZOOM("key.zume.zoom", GLFW.GLFW_KEY_Z),
	ZOOM_IN("key.zume.zoom_in", GLFW.GLFW_KEY_EQUAL),
	ZOOM_OUT("key.zume.zoom_out", GLFW.GLFW_KEY_MINUS),
	
	;
	
	public final KeyBinding value;
	
	public boolean isPressed() {
		return value.isPressed();
	}
	
	ZumeKeyBind(String translationKey, InputUtil.Type type, int code, String category) {
		this.value = new KeyBinding(translationKey, type, code, category);
	}
	
	ZumeKeyBind(String translationKey, int code) {
		this(translationKey, InputUtil.Type.KEYSYM, code, "category.zume");
	}
	
}
