package dev.nolij.zume.lexforge;

import dev.nolij.zume.common.CameraPerspective;
import dev.nolij.zume.common.IZumeImplementation;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.common.util.MethodHandleHelper;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

@Mod(Zume.MOD_ID)
public class LexZume implements IZumeImplementation {
	
	public LexZume() {
		if (!FMLEnvironment.dist.isClient())
			return;
		
		Zume.LOGGER.info("Loading LexZume...");
		
		LexZumeConfigScreen.register();
		
		Zume.init(this, FMLPaths.CONFIGDIR.get());
		if (Zume.disabled)
			return;
		
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerKeyBindings);
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
	
	private void registerKeyBindings(RegisterKeyMappingsEvent event) {
		for (final ZumeKeyBind keyBind : ZumeKeyBind.values()) {
			event.register(keyBind.value);
		}
	}
	
	private void render(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			Zume.render();
		}
	}
	
	private void calculateFOV(ViewportEvent.ComputeFov event) {
		if (Zume.shouldHookFOV()) {
			event.setFOV(Zume.transformFOV(event.getFOV()));
		}
	}
	
	@SuppressWarnings("DataFlowIssue")
	@NotNull
	private static final MethodHandle GET_SCROLL_DELTA = MethodHandleHelper.firstNonNull(
		MethodHandleHelper.getMethodOrNull(InputEvent.MouseScrollingEvent.class, "getScrollDelta"),
		MethodHandleHelper.getMethodOrNull(InputEvent.MouseScrollingEvent.class, "getDeltaY")
	);
	
	private void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        final int scrollAmount;
        try {
	        scrollAmount = (int) (double) GET_SCROLL_DELTA.invokeExact(event);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
        if (Zume.interceptScroll(scrollAmount)) {
			event.setCanceled(true);
		}
	}
	
}
