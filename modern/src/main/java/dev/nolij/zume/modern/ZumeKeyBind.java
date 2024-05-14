package dev.nolij.zume.modern;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public enum ZumeKeyBind {
	
	ZOOM("zume.zoom", GLFW.GLFW_KEY_Z),
	ZOOM_IN("zume.zoom_in", GLFW.GLFW_KEY_EQUAL),
	ZOOM_OUT("zume.zoom_out", GLFW.GLFW_KEY_MINUS),
	
	;
	
	public final KeyMapping value;
	
	public boolean isPressed() {
		return value.isDown();
	}
	
	ZumeKeyBind(String translationKey, InputConstants.Type type, int code, String category) {
		this.value = new KeyMapping(translationKey, type, code, category);
	}
	
	ZumeKeyBind(String translationKey, int code) {
		this(translationKey, InputConstants.Type.KEYSYM, code, "zume");
	}
	
}
