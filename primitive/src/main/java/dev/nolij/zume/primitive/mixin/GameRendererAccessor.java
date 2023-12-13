package dev.nolij.zume.primitive.mixin;

import net.minecraft.client.util.Smoother;
import net.minecraft.sortme.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
	
	@Accessor("cinematicYawSmoother")
	void setCinematicYawSmoother(Smoother value);
	@Accessor("cinematicPitchSmoother")
	void setCinematicPitchSmoother(Smoother value);
	
}
