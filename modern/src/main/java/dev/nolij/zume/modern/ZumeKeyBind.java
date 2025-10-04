package dev.nolij.zume.modern;

import com.mojang.blaze3d.platform.InputConstants;
import dev.nolij.zume.api.util.v1.MethodHandleHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public enum ZumeKeyBind {
	
	ZOOM("zume.zoom", GLFW.GLFW_KEY_Z),
	ZOOM_IN("zume.zoom_in", GLFW.GLFW_KEY_EQUAL),
	ZOOM_OUT("zume.zoom_out", GLFW.GLFW_KEY_MINUS),
	
	;
	
	private static class ConstructorHolder {
		private static final MethodHandle KEYMAPPING_CONSTRUCTOR = buildKeymappingConstructor();
		
		private static MethodHandle buildKeymappingConstructor() {
			var constructorHandle = MethodHandleHelper.PUBLIC.getConstructorOrNull(KeyMapping.class, MethodType.methodType(
				KeyMapping.class, String.class, InputConstants.Type.class, int.class, String.class
			));
			if (constructorHandle != null) {
				return MethodHandles.insertArguments(constructorHandle, 3, "zume");
			}
			String categoryClassName = FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary",
				"net.minecraft.class_304$class_11900");
			Class<?> keybindingCategory;
			try {
				keybindingCategory = Class.forName(categoryClassName);
			} catch (ReflectiveOperationException e) {
				throw new UnsupportedOperationException("Cannot find KeyMapping.Category");
			}
			String miscFieldName = FabricLoader.getInstance().getMappingResolver().mapFieldName("intermediary",
				"net.minecraft.class_304.class_11900", "field_62556", "Lnet/minecraft/class_304$class_11900;");
			Object miscCategory;
			try {
				miscCategory = keybindingCategory.getDeclaredField(miscFieldName).get(null);
			} catch (Exception e) {
				throw new UnsupportedOperationException("Cannot find 1.21.9+ keybind logic");
			}
			// try 1.21.9+ constructor
			var newConstructorHandle = MethodHandleHelper.PUBLIC.getConstructorOrNull(KeyMapping.class,
				MethodType.methodType(
					KeyMapping.class, String.class, InputConstants.Type.class, int.class, keybindingCategory
				));
			if (newConstructorHandle == null) {
				throw new UnsupportedOperationException("Cannot find 1.21.9+ KeyMapping constructor");
			}
			// Inject category into handle
			return MethodHandles.insertArguments(newConstructorHandle, 3, miscCategory);
		}
	}
	
	public final KeyMapping value;
	
	public boolean isPressed() {
		return value.isDown();
	}
	
	ZumeKeyBind(String translationKey, int code) {
		try {
			this.value = (KeyMapping)ConstructorHolder.KEYMAPPING_CONSTRUCTOR.invoke(translationKey,
				InputConstants.Type.KEYSYM,
				code);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
