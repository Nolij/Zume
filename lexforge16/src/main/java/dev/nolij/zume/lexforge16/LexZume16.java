package dev.nolij.zume.lexforge16;

import cpw.mods.modlauncher.api.INameMappingService;
import dev.nolij.zume.common.CameraPerspective;
import dev.nolij.zume.common.IZumeImplementation;
import dev.nolij.zume.common.Zume;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@Mod(Zume.MOD_ID)
public class LexZume16 implements IZumeImplementation {
	
	public LexZume16() {
		if (!FMLEnvironment.dist.isClient())
			return;
		
		Zume.LOGGER.info("Loading LexZume16...");
		
		LexZume16ConfigScreen.register();
		
		Zume.init(this, FMLPaths.CONFIGDIR.get());
		if (Zume.disabled)
			return;
		
		for (final ZumeKeyBind keyBind : ZumeKeyBind.values()) {
			ClientRegistry.registerKeyBinding(keyBind.value);
		}
		
		MinecraftForge.EVENT_BUS.addListener(this::render);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::calculateFOV);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onMouseScroll);
	}
	
	@Override
	public boolean isZoomPressed() {
		return Minecraft.getInstance().screen == null && ZumeKeyBind.ZOOM.isPressed();
	}
	
	@Override
	public boolean isZoomInPressed() {
		return ZumeKeyBind.ZOOM_IN.isPressed();
	}
	
	@Override
	public boolean isZoomOutPressed() {
		return ZumeKeyBind.ZOOM_OUT.isPressed();
	}
	
	private static final MethodHandle GET_CAMERA_TYPE;
	private static final MethodHandle ORDINAL;
	private static final MethodHandle THIRD_PERSON_VIEW;
	
	static {
		final MethodHandles.Lookup lookup = MethodHandles.lookup();
		
		MethodHandle getCameraType = null;
		MethodHandle ordinal = null;
		MethodHandle thirdPersonView = null;
		
		try {
			final String getCameraTypeName = ObfuscationReflectionHelper.remapName(
				INameMappingService.Domain.METHOD, "func_243230_g");
			
			getCameraType = lookup.unreflect(Options.class.getMethod(getCameraTypeName))
				.asType(MethodType.methodType(Enum.class, Options.class));
			
			ordinal = lookup.unreflect(Enum.class.getMethod("ordinal"))
				.asType(MethodType.methodType(int.class, Enum.class));
		} catch (NoSuchMethodException ignored) {
			try {
				//noinspection JavaLangInvokeHandleSignature
				thirdPersonView = lookup.findGetter(Options.class, "field_74320_O", int.class);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				throw new AssertionError(e);
			}
		} catch (IllegalAccessException e) {
			throw new AssertionError(e);
		}
		
		GET_CAMERA_TYPE = getCameraType;
		ORDINAL = ordinal;
		THIRD_PERSON_VIEW = thirdPersonView;
	}
	
	@Override
	public CameraPerspective getCameraPerspective() {
		int ordinal;
		try {
			if (GET_CAMERA_TYPE != null)
				ordinal = (int) ORDINAL.invokeExact((Enum<?>) GET_CAMERA_TYPE.invokeExact(Minecraft.getInstance().options));
			else
				ordinal = (int) THIRD_PERSON_VIEW.invokeExact(Minecraft.getInstance().options);
		} catch (Throwable e) {
			throw new AssertionError(e);
		}
		
		return CameraPerspective.values()[ordinal];
	}
	
	private void render(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			Zume.render();
		}
	}
	
	private void calculateFOV(EntityViewRenderEvent.FOVModifier event) {
		if (Zume.shouldHookFOV()) {
			event.setFOV(Zume.transformFOV(event.getFOV()));
		}
	}
	
	private void onMouseScroll(InputEvent.MouseScrollEvent event) {
		final int scrollAmount = (int) event.getScrollDelta();
		if (Zume.interceptScroll(scrollAmount)) {
			event.setCanceled(true);
		}
	}
	
}
