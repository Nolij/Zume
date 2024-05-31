package dev.nolij.zume.mixin.legacy;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.SmoothUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
	
	@Accessor("cursorXSmoother")
	void setCursorXSmoother(SmoothUtil value);
	
	@Accessor("cursorYSmoother")
	void setCursorYSmoother(SmoothUtil value);
	
	@Accessor("cursorDeltaX")
	void setCursorDeltaX(float value);
	
	@Accessor("cursorDeltaY")
	void setCursorDeltaY(float value);
	
	@Accessor("smoothedCursorDeltaX")
	void setSmoothedCursorDeltaX(float value);
	
	@Accessor("smoothedCursorDeltaY")
	void setSmoothedCursorDeltaY(float value);
	
	@Accessor("lastTickDelta")
	void setLastTickDelta(float value);
	
}
