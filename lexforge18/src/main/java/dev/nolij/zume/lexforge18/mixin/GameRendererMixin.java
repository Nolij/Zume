package dev.nolij.zume.lexforge18.mixin;

import dev.nolij.zume.common.Zume;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	
	@Inject(method = "getFov", at = @At("TAIL"), cancellable = true)
	public void zume$getFov$TAIL(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
		if (Zume.isZooming()) {
			cir.setReturnValue(Zume.transformFOV(cir.getReturnValueD()));
		}
	}
	
}
