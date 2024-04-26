package dev.nolij.zume.neoforge;

import dev.nolij.zume.api.platform.v0.CameraPerspective;
import dev.nolij.zume.api.platform.v0.IZumeImplementation;
import dev.nolij.zume.api.platform.v0.ZumeAPI;
import dev.nolij.zume.api.config.v0.ZumeConfigAPI;
import dev.nolij.zume.api.util.v0.MethodHandleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import net.neoforged.neoforge.client.event.CalculatePlayerTurnEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static dev.nolij.zume.impl.ZumeConstants.MOD_ID;

@Mod(MOD_ID)
public class NeoZume implements IZumeImplementation {
	
	private static final Class<?> CONFIG_SCREEN_EXT_INTERFACE = MethodHandleHelper.getClassOrNull(
		NeoZume.class.getClassLoader(), "net.neoforged.neoforge.client.gui.IConfigScreenFactory");
	private static final Class<?> CONFIG_SCREEN_EXT_RECORD = MethodHandleHelper.getClassOrNull(
		NeoZume.class.getClassLoader(), "net.neoforged.neoforge.client.ConfigScreenHandler$ConfigScreenFactory");
	private static final Class<?> CONFIG_SCREEN_EXT = MethodHandleHelper.firstNonNull(
		CONFIG_SCREEN_EXT_INTERFACE,
		CONFIG_SCREEN_EXT_RECORD
	);
	private static final MethodHandle REGISTER_EXT_POINT = MethodHandleHelper.getMethodOrNull(
		ModContainer.class,
		"registerExtensionPoint",
		Class.class, Supplier.class
	);
	
	public NeoZume(IEventBus modEventBus, ModContainer modContainer) {
		if (!FMLEnvironment.dist.isClient())
			return;
		
		ZumeAPI.getLogger().info("Loading NeoZume...");
		
		try {
			REGISTER_EXT_POINT.invokeExact(modContainer, CONFIG_SCREEN_EXT, (Supplier<?>) () -> {
				try {
					if (CONFIG_SCREEN_EXT_RECORD == null) {
						return NeoZumeConfigScreenFactory.class.getDeclaredConstructor().newInstance();
					} else {
						return CONFIG_SCREEN_EXT_RECORD
							.getDeclaredConstructor(BiFunction.class)
							.newInstance((BiFunction<Minecraft, Screen, Screen>) (minecraft, parent) ->
								new NeoZumeConfigScreen(parent));
					}
				} catch (InstantiationException | IllegalAccessException | 
				         InvocationTargetException | NoSuchMethodException e) {
					throw new AssertionError(e);
				}
			});
		} catch (Throwable e) {
			throw new AssertionError(e);
		}
		
		ZumeAPI.registerImplementation(this, FMLPaths.CONFIGDIR.get());
		if (ZumeConfigAPI.isDisabled())
			return;
		
		modEventBus.addListener(this::registerKeyBindings);
		NeoForge.EVENT_BUS.addListener(this::render);
		NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::calculateFOV);
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::calculateTurnPlayerValues);
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onMouseScroll);
		NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::calculateDetachedCameraDistance);
	}
	
	@Override
	public boolean isZoomPressed() {
		return ZumeKeyBind.ZOOM.isPressed();
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
			ZumeAPI.renderHook();
		}
	}
	
	private void calculateFOV(ViewportEvent.ComputeFov event) {
		if (ZumeAPI.isFOVHookActive()) {
			event.setFOV(ZumeAPI.fovHook(event.getFOV()));
		}
	}
	
	private void calculateTurnPlayerValues(CalculatePlayerTurnEvent event) {
		event.setMouseSensitivity(ZumeAPI.mouseSensitivityHook(event.getMouseSensitivity()));
		event.setCinematicCameraEnabled(ZumeAPI.cinematicCameraEnabledHook(event.getCinematicCameraEnabled()));
	}
	
	private void onMouseScroll(InputEvent.MouseScrollingEvent event) {
		final int scrollAmount = (int) event.getScrollDeltaY();
		if (ZumeAPI.mouseScrollHook(scrollAmount)) {
			event.setCanceled(true);
		}
	}
	
	private void calculateDetachedCameraDistance(CalculateDetachedCameraDistanceEvent event) {
        event.setDistance(ZumeAPI.thirdPersonCameraHook(event.getDistance()));
	}
	
}
