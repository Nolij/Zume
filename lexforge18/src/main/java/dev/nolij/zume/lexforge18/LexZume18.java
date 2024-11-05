package dev.nolij.zume.lexforge18;

import dev.nolij.libnolij.refraction.Refraction;
import dev.nolij.zume.impl.CameraPerspective;
import dev.nolij.zume.impl.IZumeImplementation;
import dev.nolij.zume.impl.Zume;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static dev.nolij.zume.impl.ZumeConstants.MOD_ID;

@Mod(MOD_ID)
public class LexZume18 implements IZumeImplementation {
	
	private static final Class<?> FOV_EVENT_CLASS = Refraction.safe().getClassOrNull(
		"net.minecraftforge.client.event.EntityViewRenderEvent$FieldOfView",
		"net.minecraftforge.client.event.EntityViewRenderEvent$FOVModifier"
	);
	private static final MethodHandle GET_FOV = Refraction.safe().getMethodOrNull(
		FOV_EVENT_CLASS,
		"getFOV",
		MethodType.methodType(double.class, EntityViewRenderEvent.class)
	);
	private static final MethodHandle SET_FOV = Refraction.safe().getMethodOrNull(
		FOV_EVENT_CLASS,
		"setFOV",
		MethodType.methodType(void.class, EntityViewRenderEvent.class, double.class),
		double.class
	);
	
	public LexZume18() {
		if (!FMLEnvironment.dist.isClient())
			return;
		
		Zume.LOGGER.info("Loading LexZume18...");
		
		LexZume18ConfigScreen.register();
		
		Zume.registerImplementation(this, FMLPaths.CONFIGDIR.get());
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
	
	@Override
	public CameraPerspective getCameraPerspective() {
		return CameraPerspective.values()[Minecraft.getInstance().options.getCameraType().ordinal()];
	}
	
	private void render(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			Zume.renderHook();
		}
	}
	
	private void calculateFOV(EntityViewRenderEvent event) {
		if (event.getClass() == FOV_EVENT_CLASS && Zume.isFOVHookActive()) {
			//noinspection DataFlowIssue
			SET_FOV.invokeExact(event, Zume.fovHook((double) GET_FOV.invokeExact(event)));
		}
	}
	
	private void onMouseScroll(InputEvent.MouseScrollEvent event) {
		if (Zume.mouseScrollHook((int) event.getScrollDelta())) {
			event.setCanceled(true);
		}
	}
	
}
