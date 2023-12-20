package dev.nolij.zume.archaic;

import dev.nolij.zume.archaic.mixin.EntityRendererAccessor;
import dev.nolij.zume.common.IZumeProvider;
import dev.nolij.zume.common.Zume;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.MouseFilter;

import java.io.File;

@Mod(
	modid = Zume.MOD_ID,
	name = Tags.MOD_NAME,
	version = Tags.MOD_VERSION, 
	acceptedMinecraftVersions = Tags.VERSION_RANGE,
	guiFactory = "dev.nolij.zume.legacyforge.ArchaicConfigProvider",
	dependencies = "required-after:unimixins@*")
public class ArchaicZume implements IZumeProvider {
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Zume.LOGGER.info("Loading Archaic Zume...");
		
		for (final ZumeKeyBind keyBind : ZumeKeyBind.values()) {
			ClientRegistry.registerKeyBinding(keyBind.value);
		}
		
		Zume.init(this, new File(Launch.minecraftHome, "config" + File.separator + Zume.CONFIG_FILE_NAME));
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
	public void onZoomActivate() {
		if (Zume.CONFIG.enableCinematicZoom && !Minecraft.getMinecraft().gameSettings.smoothCamera) {
			final EntityRendererAccessor entityRenderer = (EntityRendererAccessor) Minecraft.getMinecraft().entityRenderer;
			entityRenderer.setMouseFilterXAxis(new MouseFilter());
			entityRenderer.setMouseFilterYAxis(new MouseFilter());
			entityRenderer.setSmoothCamYaw(0F);
			entityRenderer.setSmoothCamPitch(0F);
			entityRenderer.setSmoothCamFilterX(0F);
			entityRenderer.setSmoothCamFilterY(0F);
			entityRenderer.setSmoothCamPartialTicks(0F);
		}
	}
	
}
