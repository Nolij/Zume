package dev.nolij.zume.neoforge;

import dev.nolij.zume.api.util.v1.MethodHandleHelper;
import dev.nolij.zume.impl.CameraPerspective;
import dev.nolij.zume.impl.IZumeImplementation;
import dev.nolij.zume.impl.Zume;
import dev.nolij.zume.neoforge.integration.embeddium.ZumeEmbeddiumConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static dev.nolij.zume.impl.ZumeConstants.MOD_ID;

@Mod(value = MOD_ID, dist = Dist.CLIENT)
public class NeoZume implements IZumeImplementation {
	
	private static final MethodHandleHelper METHOD_HANDLE_HELPER =
		new MethodHandleHelper(NeoZume.class.getClassLoader(), MethodHandles.lookup());
	
	private static final Class<?> CONFIG_SCREEN_EXT_INTERFACE = METHOD_HANDLE_HELPER.getClassOrNull(
		"net.neoforged.neoforge.client.gui.IConfigScreenFactory");
	private static final Class<?> CONFIG_SCREEN_EXT_RECORD = METHOD_HANDLE_HELPER.getClassOrNull(
		"net.neoforged.neoforge.client.ConfigScreenHandler$ConfigScreenFactory");
	private static final Class<?> CONFIG_SCREEN_EXT = MethodHandleHelper.firstNonNull(
		CONFIG_SCREEN_EXT_INTERFACE, CONFIG_SCREEN_EXT_RECORD);
	private static final MethodHandle REGISTER_EXT_POINT = METHOD_HANDLE_HELPER.getMethodOrNull(
		ModContainer.class, "registerExtensionPoint", Class.class, Supplier.class
	);
	
	private static final Class<?> RENDER_TICK_EVENT = METHOD_HANDLE_HELPER.getClassOrNull(
		"net.neoforged.neoforge.event.TickEvent$RenderTickEvent");
	private static final Class<?> TICK_EVENT_PHASE = METHOD_HANDLE_HELPER.getClassOrNull(
		"net.neoforged.neoforge.event.TickEvent$Phase");
	private static final Enum<?> TICK_EVENT_PHASE_START;
	static {
		if (TICK_EVENT_PHASE == null) {
			TICK_EVENT_PHASE_START = null;
		} else {
			try {
				TICK_EVENT_PHASE_START = (Enum<?>) TICK_EVENT_PHASE.getField("START").get(null);
			} catch (IllegalAccessException | NoSuchFieldException e) {
				throw new AssertionError(e);
			}
		}
	}
	private static final MethodHandle RENDER_TICK_EVENT_PHASE_GETTER = METHOD_HANDLE_HELPER.getGetterOrNull(
		RENDER_TICK_EVENT, "phase", TICK_EVENT_PHASE, MethodType.methodType(Enum.class, Object.class));
	private static final Class<?> RENDER_FRAME_EVENT = METHOD_HANDLE_HELPER.getClassOrNull(
		"net.neoforged.neoforge.client.event.RenderFrameEvent$Pre");
	
	public NeoZume(IEventBus modEventBus, ModContainer modContainer) {
		if (!FMLEnvironment.dist.isClient())
			return;
		
		Zume.LOGGER.info("Loading NeoZume...");
		
		if (REGISTER_EXT_POINT != null &&
			CONFIG_SCREEN_EXT != null &&
			(CONFIG_SCREEN_EXT_RECORD != null || CONFIG_SCREEN_EXT_INTERFACE != null)) {
			try {
				REGISTER_EXT_POINT.invokeExact(modContainer, CONFIG_SCREEN_EXT, (Supplier<?>) () -> {
					try {
						if (CONFIG_SCREEN_EXT_INTERFACE != null) {
							return NeoZumeConfigScreenFactory.class
								.getDeclaredConstructor()
								.newInstance();
						} else //noinspection ConstantValue,UnreachableCode
							if (CONFIG_SCREEN_EXT_RECORD != null) {
							return CONFIG_SCREEN_EXT_RECORD
								.getDeclaredConstructor(BiFunction.class)
								.newInstance((BiFunction<Minecraft, Screen, Screen>) (minecraft, parent) ->
									new NeoZumeConfigScreen(parent));
						} else {
							return null;
						}
					} catch (ReflectiveOperationException e) {
						throw new RuntimeException(e);
					}
				});
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		
		Zume.registerImplementation(this, FMLPaths.CONFIGDIR.get());
		if (Zume.config.disable)
			return;
		
		modEventBus.addListener(this::registerKeyBindings);
		if (RENDER_FRAME_EVENT != null) {
			//noinspection unchecked
			NeoForge.EVENT_BUS.addListener((Class<? extends Event>) RENDER_FRAME_EVENT, this::render);
		} else if (
			RENDER_TICK_EVENT != null && 
			RENDER_TICK_EVENT_PHASE_GETTER != null && 
			TICK_EVENT_PHASE_START != null) {
			//noinspection unchecked
			NeoForge.EVENT_BUS.addListener((Class<? extends Event>) RENDER_TICK_EVENT, this::renderLegacy);
		} else {
			throw new AssertionError("NeoZume doesn't support this version of NeoForge");
		}
		NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::calculateFOV);
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::calculateTurnPlayerValues);
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onMouseScroll);
		NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::calculateDetachedCameraDistance);
		
		if (METHOD_HANDLE_HELPER.getClassOrNull("org.embeddedt.embeddium.api.options.OptionIdentifier") != null) {
			new ZumeEmbeddiumConfigScreen();
		}
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
	
	private void render(Object event) {
		Zume.renderHook();
	}
	
	private void renderLegacy(Object event) {
		try {
			//noinspection DataFlowIssue
			if ((Enum<?>) RENDER_TICK_EVENT_PHASE_GETTER.invokeExact(event) == TICK_EVENT_PHASE_START) {
				Zume.renderHook();
			}
		} catch (Throwable e) {
			throw new AssertionError(e);
		}
	}
	
	private void calculateFOV(ViewportEvent.ComputeFov event) {
		if (Zume.isFOVHookActive()) {
			event.setFOV(Zume.fovHook(event.getFOV()));
		}
	}
	
	private void calculateTurnPlayerValues(CalculatePlayerTurnEvent event) {
		event.setMouseSensitivity(Zume.mouseSensitivityHook(event.getMouseSensitivity()));
		event.setCinematicCameraEnabled(Zume.cinematicCameraEnabledHook(event.getCinematicCameraEnabled()));
	}
	
	private void onMouseScroll(InputEvent.MouseScrollingEvent event) {
		final int scrollAmount = (int) event.getScrollDeltaY();
		if (Zume.mouseScrollHook(scrollAmount)) {
			event.setCanceled(true);
		}
	}
	
	@SuppressWarnings("DataFlowIssue")
	private static final MethodHandle SET_DISTANCE = MethodHandleHelper.firstNonNull(
		METHOD_HANDLE_HELPER.getMethodOrNull(CalculateDetachedCameraDistanceEvent.class, "setDistance", float.class),
		METHOD_HANDLE_HELPER.getMethodOrNull(CalculateDetachedCameraDistanceEvent.class, "setDistance", double.class)
	).asType(MethodType.methodType(void.class, float.class));
	
	private void calculateDetachedCameraDistance(CalculateDetachedCameraDistanceEvent event) {
		try {
			SET_DISTANCE.invokeExact(event, (float) Zume.thirdPersonCameraHook(event.getDistance()));
		} catch (Throwable e) {
			throw new AssertionError(e);
		}
	}
	
}
