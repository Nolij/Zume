package dev.nolij.zume.mixin.primitive;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.SmoothFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
	
	@Accessor("smoothTurnX")
	void setSmoothTurnX(SmoothFloat value);
	@Accessor("smoothTurnY")
	void setSmoothTurnY(SmoothFloat value);
	
}
