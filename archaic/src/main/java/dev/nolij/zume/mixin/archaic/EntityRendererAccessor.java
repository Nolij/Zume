package dev.nolij.zume.mixin.archaic;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.MouseFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityRenderer.class)
public interface EntityRendererAccessor {
	
	@Accessor("smoothCamFilterX")
	void setSmoothCamFilterX(float value);
	
	@Accessor("smoothCamFilterY")
	void setSmoothCamFilterY(float value);
	
	@Accessor("smoothCamYaw")
	void setSmoothCamYaw(float value);
	
	@Accessor("smoothCamPitch")
	void setSmoothCamPitch(float value);
	
	@Accessor("smoothCamPartialTicks")
	void setSmoothCamPartialTicks(float value);
	
	@Accessor("mouseFilterXAxis")
	void setMouseFilterXAxis(MouseFilter value);
	
	@Accessor("mouseFilterYAxis")
	void setMouseFilterYAxis(MouseFilter value);
	
}
