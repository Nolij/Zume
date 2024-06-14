package dev.nolij.zume.lexforge;

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
	
	ZumeKeyBind(String translationKey, int code) {
		this.value = new KeyMapping(translationKey, InputConstants.Type.KEYSYM, code, "zume");
	}
	
}
