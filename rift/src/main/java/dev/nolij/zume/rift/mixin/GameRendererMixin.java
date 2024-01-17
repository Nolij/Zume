package dev.nolij.zume.rift.mixin;

import dev.nolij.zume.common.Zume;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	
	@Shadow
	protected abstract double getFOVModifier(float par1, boolean par2);
	
	@Inject(method = "updateCameraAndRender(FJZ)V", at = @At("HEAD"))
	public void zume$render$HEAD(float f, long l, boolean renderWorldIn, CallbackInfo ci) {
		Zume.render();
	}
	
	@Redirect(
		method = {
			"setupCameraTransform",
			"renderHand",
			"updateCameraAndRender(FJ)V",
			"renderCloudsCheck"}, 
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;getFOVModifier(FZ)D"))
	private double zume$getFov$TAIL(GameRenderer instance, float v, boolean b) {
		final double original = getFOVModifier(v, b);
		
		if (Zume.isActive()) {
			return Zume.transformFOV(original);
		}
		
		return original;
	}
	
}
