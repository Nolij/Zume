package dev.nolij.zume.lexforge;

import dev.nolij.zume.api.platform.v0.CameraPerspective;
import dev.nolij.zume.api.platform.v0.IZumeImplementation;
import dev.nolij.zume.api.platform.v0.ZumeAPI;
import dev.nolij.zume.api.config.v1.ZumeConfigAPI;
import dev.nolij.zume.api.util.v0.MethodHandleHelper;
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

import static dev.nolij.zume.impl.ZumeConstants.MOD_ID;

@Mod(MOD_ID)
public class LexZume implements IZumeImplementation {
	
	public LexZume() {
		if (!FMLEnvironment.dist.isClient())
			return;
		
		ZumeAPI.getLogger().info("Loading LexZume...");
		
		LexZumeConfigScreen.register();
		
		ZumeAPI.registerImplementation(this, FMLPaths.CONFIGDIR.get());
		if (ZumeConfigAPI.isDisabled())
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
	public @NotNull CameraPerspective getCameraPerspective() {
		return CameraPerspective.values()[Minecraft.getInstance().options.getCameraType().ordinal()];
	}
	
	private void registerKeyBindings(RegisterKeyMappingsEvent event) {
		for (final ZumeKeyBind keyBind : ZumeKeyBind.values()) {
			event.register(keyBind.value);
		}
	}
	
	private void render(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			ZumeAPI.renderHook();
		}
	}
	
	private void calculateFOV(ViewportEvent.ComputeFov event) {
		if (ZumeAPI.isFOVHookActive()) {
			event.setFOV(ZumeAPI.fovHook(event.getFOV()));
		}
	}
	
	@SuppressWarnings("DataFlowIssue")
	@NotNull
	private static final MethodHandle GET_SCROLL_DELTA = MethodHandleHelper.firstNonNull(
		MethodHandleHelper.PUBLIC.getMethodOrNull(InputEvent.MouseScrollingEvent.class, "getScrollDelta"),
		MethodHandleHelper.PUBLIC.getMethodOrNull(InputEvent.MouseScrollingEvent.class, "getDeltaY")
	);
	
	private void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        final int scrollAmount;
        try {
	        scrollAmount = (int) (double) GET_SCROLL_DELTA.invokeExact(event);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
        if (ZumeAPI.mouseScrollHook(scrollAmount)) {
			event.setCanceled(true);
		}
	}
	
}
