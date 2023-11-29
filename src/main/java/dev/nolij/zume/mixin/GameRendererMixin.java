package dev.nolij.zume.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.nolij.zume.Zume;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	
	@Inject(method = "render", at = @At("HEAD"))
	public void zume$render$HEAD(CallbackInfo ci) {
		Zume.render();
	}
	
	@Inject(method = "getFov", at = @At("TAIL"), cancellable = true)
	public void zume$getFov$TAIL(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
		Zume.realFOV = cir.getReturnValueD();
		if (Zume.isActive()) {
			cir.setReturnValue(Zume.getFOV());
		}
	}
	
}
