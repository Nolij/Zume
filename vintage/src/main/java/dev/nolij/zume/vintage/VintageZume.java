package dev.nolij.zume.vintage;

import dev.nolij.zume.common.CameraPerspective;
import dev.nolij.zume.common.Constants;
import dev.nolij.zume.common.IZumeImplementation;
import dev.nolij.zume.common.Zume;
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

import java.io.File;

@Mod(
	modid = Zume.MOD_ID,
	name = Constants.MOD_NAME,
	version = Constants.MOD_VERSION, 
	acceptedMinecraftVersions = Constants.VINTAGE_VERSION_RANGE,
	guiFactory = "dev.nolij.zume.vintage.VintageConfigProvider")
public class VintageZume implements IZumeImplementation {
	
	public VintageZume() {
		if (!FMLLaunchHandler.side().isClient())
			return;
		
		Zume.LOGGER.info("Loading Vintage Zume...");
		
		Zume.init(this, new File(Launch.minecraftHome, "config").toPath());
		if (Zume.disabled)
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
	public CameraPerspective getCameraPerspective() {
		return CameraPerspective.values()[Minecraft.getMinecraft().gameSettings.thirdPersonView];
	}
	
	@SubscribeEvent
	public void render(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			Zume.render();
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void calculateFOV(EntityViewRenderEvent.FOVModifier event) {
		if (Zume.shouldHookFOV()) {
			event.setFOV((float) Zume.transformFOV(event.getFOV()));
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void mouseEvent(MouseEvent mouseEvent) {
		final int scrollAmount = mouseEvent.getDwheel();
		if (Zume.interceptScroll(scrollAmount)) {
			mouseEvent.setCanceled(true);
		}
	}
	
}
