package dev.nolij.zume.modern;

import dev.nolij.zume.api.platform.v0.CameraPerspective;
import dev.nolij.zume.api.platform.v0.IZumeImplementation;
import dev.nolij.zume.api.platform.v0.ZumeAPI;
import dev.nolij.zume.api.config.v0.ZumeConfigAPI;
import dev.nolij.zume.api.util.v0.MethodHandleHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class ModernZume implements ClientModInitializer, IZumeImplementation {
	
	@Override
	public void onInitializeClient() {
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT)
			return;
		
		ZumeAPI.getLogger().info("Loading Modern Zume...");
		
		ZumeAPI.registerImplementation(this, FabricLoader.getInstance().getConfigDir());
		if (ZumeConfigAPI.isDisabled()) return;
		
		for (final ZumeKeyBind keyBind : ZumeKeyBind.values()) {
			KeyBindingHelper.registerKeyBinding(keyBind.value);
		}
	}
	
	@Override
	public boolean isZoomPressed() {
		return MinecraftClient.getInstance().currentScreen == null && ZumeKeyBind.ZOOM.isPressed();
	}
	
	@Override
	public boolean isZoomInPressed() {
		return ZumeKeyBind.ZOOM_IN.isPressed();
	}
	
	@Override
	public boolean isZoomOutPressed() {
		return ZumeKeyBind.ZOOM_OUT.isPressed();
	}
	
	private static final MethodHandle GET_PERSPECTIVE = MethodHandleHelper.PUBLIC.getMethodOrNull(
		GameOptions.class,
		FabricLoader.getInstance().getMappingResolver().mapMethodName("intermediary",
			"net.minecraft.class_315", "method_31044", "()Lnet/minecraft/class_5498;"),
		MethodType.methodType(Enum.class, GameOptions.class));
	private static final MethodHandle PERSPECTIVE =
		MethodHandleHelper.PUBLIC.getGetterOrNull(GameOptions.class, "field_1850", int.class);
	
	@Override
	public @NotNull CameraPerspective getCameraPerspective() {
		int ordinal;
		try {
			if (GET_PERSPECTIVE != null)
				ordinal = ((Enum<?>) GET_PERSPECTIVE.invokeExact(MinecraftClient.getInstance().options)).ordinal();
			else
				ordinal = (int) PERSPECTIVE.invokeExact(MinecraftClient.getInstance().options);
		} catch (Throwable e) {
			throw new AssertionError(e);
		}
		
		return CameraPerspective.values()[ordinal];
	}
	
}
