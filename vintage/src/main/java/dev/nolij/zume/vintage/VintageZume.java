package dev.nolij.zume.vintage;

import dev.nolij.zume.api.platform.v0.CameraPerspective;
import dev.nolij.zume.api.platform.v0.IZumeImplementation;
import dev.nolij.zume.api.platform.v0.ZumeAPI;
import dev.nolij.zume.api.config.v0.ZumeConfigAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static dev.nolij.zume.impl.ZumeConstants.*;

@Mod(
	modid = MOD_ID,
	name = MOD_NAME,
	version = MOD_VERSION, 
	acceptedMinecraftVersions = VINTAGE_VERSION_RANGE,
	guiFactory = "dev.nolij.zume.vintage.VintageConfigProvider")
public class VintageZume implements IZumeImplementation {
	
	public VintageZume() {
		if (!FMLLaunchHandler.side().isClient())
			return;
		
		ZumeAPI.getLogger().info("Loading Vintage Zume...");
		
		ZumeAPI.registerImplementation(this, new File(Launch.minecraftHome, "config").toPath());
		if (ZumeConfigAPI.isDisabled())
			return;
		
		for (final ZumeKeyBind keyBind : ZumeKeyBind.values()) {
			ClientRegistry.registerKeyBinding(keyBind.value);
		}
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public boolean isZoomPressed() {
		return Minecraft.getMinecraft().currentScreen == null && ZumeKeyBind.ZOOM.isPressed();
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
		return CameraPerspective.values()[Minecraft.getMinecraft().gameSettings.thirdPersonView];
	}
	
	@SubscribeEvent
	public void render(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			ZumeAPI.renderHook();
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void calculateFOV(EntityViewRenderEvent.FOVModifier event) {
		if (ZumeAPI.isFOVHookActive()) {
			event.setFOV((float) ZumeAPI.fovHook(event.getFOV()));
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void mouseEvent(MouseEvent mouseEvent) {
		final int scrollAmount = mouseEvent.getDwheel();
		if (ZumeAPI.mouseScrollHook(scrollAmount)) {
			mouseEvent.setCanceled(true);
		}
	}
	
}
