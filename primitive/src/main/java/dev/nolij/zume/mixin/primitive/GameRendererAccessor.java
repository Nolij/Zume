package dev.nolij.zume.mixin.primitive;

import net.minecraft.class_555;
import net.minecraft.client.util.SmoothUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_555.class)
public interface GameRendererAccessor {
	
	@Accessor("field_2353")
	void setCinematicYawSmoother(SmoothUtil value);
	@Accessor("field_2354")
	void setCinematicPitchSmoother(SmoothUtil value);
	
}
