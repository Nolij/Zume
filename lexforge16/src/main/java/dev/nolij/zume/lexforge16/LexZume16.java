package dev.nolij.zume.lexforge16;

import cpw.mods.modlauncher.api.INameMappingService;
import dev.nolij.zume.api.util.v1.MethodHandleHelper;
import dev.nolij.zume.impl.CameraPerspective;
import dev.nolij.zume.impl.IZumeImplementation;
import dev.nolij.zume.impl.Zume;
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
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static dev.nolij.zume.impl.ZumeConstants.MOD_ID;

@Mod(MOD_ID)
public class LexZume16 implements IZumeImplementation {
	
	public LexZume16() {
		if (!FMLEnvironment.dist.isClient())
			return;
		
		Zume.LOGGER.info("Loading LexZume16...");
		
		LexZume16ConfigScreen.register();
		
		Zume.registerImplementation(this, FMLPaths.CONFIGDIR.get());
		if (Zume.config.disable)
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
	
	private static final MethodHandle GET_CAMERA_TYPE = MethodHandleHelper.PUBLIC.getMethodOrNull(
		Options.class,
		ObfuscationReflectionHelper.remapName(INameMappingService.Domain.METHOD, "func_243230_g"),
		MethodType.methodType(Enum.class, Options.class)
	);
	private static final MethodHandle THIRD_PERSON_VIEW =
		MethodHandleHelper.PUBLIC.getGetterOrNull(Options.class, "field_74320_O", int.class);
	
	@Override
	public @NotNull CameraPerspective getCameraPerspective() {
		int ordinal;
		try {
			if (GET_CAMERA_TYPE != null)
				ordinal = ((Enum<?>) GET_CAMERA_TYPE.invokeExact(Minecraft.getInstance().options)).ordinal();
			else
				ordinal = (int) THIRD_PERSON_VIEW.invokeExact(Minecraft.getInstance().options);
		} catch (Throwable e) {
			throw new AssertionError(e);
		}
		
		return CameraPerspective.values()[ordinal];
	}
	
	private void render(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			Zume.renderHook();
		}
	}
	
	private void calculateFOV(EntityViewRenderEvent.FOVModifier event) {
		if (Zume.isFOVHookActive()) {
			event.setFOV(Zume.fovHook(event.getFOV()));
		}
	}
	
	private void onMouseScroll(InputEvent.MouseScrollEvent event) {
		final int scrollAmount = (int) event.getScrollDelta();
		if (Zume.mouseScrollHook(scrollAmount)) {
			event.setCanceled(true);
		}
	}
	
}
