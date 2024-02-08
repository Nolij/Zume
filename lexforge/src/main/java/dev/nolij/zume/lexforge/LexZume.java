package dev.nolij.zume.lexforge;

import dev.nolij.zume.common.IZumeImplementation;
import dev.nolij.zume.common.Zume;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

@Mod(Zume.MOD_ID)
public class LexZume implements IZumeImplementation {
	
	private final Minecraft minecraft;
	
	public LexZume() {
		Zume.LOGGER.info("Loading LexZume...");
		
		Zume.init(this, new File(FMLPaths.CONFIGDIR.get().toFile(), Zume.CONFIG_FILE_NAME));
		
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerKeyBindings);
		MinecraftForge.EVENT_BUS.addListener(this::render);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::calculateFOV);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onMouseScroll);
		
		ModLoadingContext.get().registerExtensionPoint(
			ConfigScreenHandler.ConfigScreenFactory.class,
			() -> new ConfigScreenHandler.ConfigScreenFactory(((minecraft, parent) -> new Screen(Component.nullToEmpty(null)) {
				@Override
				public void tick() {
					assert minecraft != null;
					
					Zume.openConfigFile();
                    minecraft.setScreen(parent);
				}
			})));
		
		this.minecraft = Minecraft.getInstance();
	}
	
	@Override
	public boolean isZoomPressed() {
		return minecraft.screen == null && ZumeKeyBind.ZOOM.isPressed();
	}
	
	@Override
	public boolean isZoomInPressed() {
		return ZumeKeyBind.ZOOM_IN.isPressed();
	}
	
	@Override
	public boolean isZoomOutPressed() {
		return ZumeKeyBind.ZOOM_OUT.isPressed();
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
		if (Zume.isFOVModified()) {
			event.setFOV(Zume.transformFOV(event.getFOV()));
		}
	}
	
	private static final MethodHandle getScrollDelta;
	
	static {
		final MethodHandles.Lookup lookup = MethodHandles.lookup();
		Method method;
        try {
            method = InputEvent.MouseScrollingEvent.class.getMethod("getScrollDelta");
        } catch (NoSuchMethodException ignored) {
            try {
	            //noinspection JavaReflectionMemberAccess
	            method = InputEvent.MouseScrollingEvent.class.getMethod("getDeltaY");
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e);
            }
        }
        try {
            getScrollDelta = lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }
	
	private void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        final int scrollAmount;
        try {
            scrollAmount = (int) (double) getScrollDelta.invokeExact(event);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
        if (scrollAmount != 0 &&
			Zume.interceptScroll(scrollAmount)) {
			event.setCanceled(true);
		}
	}
	
}
