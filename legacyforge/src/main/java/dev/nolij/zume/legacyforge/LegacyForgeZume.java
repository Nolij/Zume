package dev.nolij.zume.legacyforge;

import dev.nolij.zume.common.IZumeProvider;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.legacyforge.mixin.EntityRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.MouseFilter;

import java.io.File;

public class LegacyForgeZume implements IZumeProvider {
    
    public void preInit() {
        Zume.init(this, new File(Launch.minecraftHome, "config" + File.separator + Zume.CONFIG_FILE_NAME));
    }
    
    @Override
    public boolean isZoomPressed() {
        return ZumeKeyBind.ZOOM.value.getIsKeyPressed();
    }
    
    @Override
    public boolean isZoomInPressed() {
        return ZumeKeyBind.ZOOM_IN.value.getIsKeyPressed();
    }
    
    @Override
    public boolean isZoomOutPressed() {
        return ZumeKeyBind.ZOOM_OUT.value.getIsKeyPressed();
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
