package dev.nolij.zume.modern;

import dev.nolij.zume.common.CameraPerspective;
import dev.nolij.zume.common.IZumeImplementation;
import dev.nolij.zume.common.Zume;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ModernZume implements ClientModInitializer, IZumeImplementation {
	
	@Override
	public void onInitializeClient() {
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT)
			return;
		
		Zume.LOGGER.info("Loading Modern Zume...");
		
		Zume.init(this, FabricLoader.getInstance().getConfigDir());
		if (Zume.disabled) return;
		
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
	
	private static final MethodHandle GET_PERSPECTIVE;
	private static final MethodHandle ORDINAL;
	private static final MethodHandle PERSPECTIVE;
	
	static {
		final MethodHandles.Lookup lookup = MethodHandles.lookup();
		final MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();
		
		MethodHandle getPerspective = null;
		MethodHandle ordinal = null;
		MethodHandle perspective = null;
		
		try {
			final String getPerspectiveName = mappingResolver.mapMethodName("intermediary", 
				"net.minecraft.class_315", "method_31044", "()Lnet/minecraft/class_5498;");
			
			getPerspective = lookup.unreflect(GameOptions.class.getMethod(getPerspectiveName))
				.asType(MethodType.methodType(Enum.class, GameOptions.class));
			
			ordinal = lookup.unreflect(Enum.class.getMethod("ordinal"))
				.asType(MethodType.methodType(int.class, Enum.class));
		} catch (NoSuchMethodException ignored) {
			try {
				//noinspection JavaLangInvokeHandleSignature
				perspective = lookup.findGetter(GameOptions.class, "field_1850", int.class);
			} catch (IllegalAccessException | NoSuchFieldException e) {
				throw new AssertionError(e);
			}
		} catch (IllegalAccessException e) {
			throw new AssertionError(e);
		}
		
		GET_PERSPECTIVE = getPerspective;
		ORDINAL = ordinal;
		PERSPECTIVE = perspective;
	}
	
	@Override
	public CameraPerspective getCameraPerspective() {
		int ordinal;
		try {
			if (GET_PERSPECTIVE != null)
				ordinal = (int) ORDINAL.invokeExact((Enum<?>) GET_PERSPECTIVE.invokeExact(MinecraftClient.getInstance().options));
			else
				ordinal = (int) PERSPECTIVE.invokeExact(MinecraftClient.getInstance().options);
		} catch (Throwable e) {
			throw new AssertionError(e);
		}
		
		return CameraPerspective.values()[ordinal];
	}
	
}
