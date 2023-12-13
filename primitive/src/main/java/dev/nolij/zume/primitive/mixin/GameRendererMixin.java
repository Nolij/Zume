package dev.nolij.zume.primitive.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.primitive.PrimitiveZume;
import net.minecraft.sortme.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	
	@Inject(method = "method_1844", at = @At("HEAD"))
	public void zume$render$HEAD(CallbackInfo ci) {
		Zume.render();
	}
	
	@Inject(method = "method_1848", at = @At("TAIL"), cancellable = true)
	public void zume$getFov$TAIL(CallbackInfoReturnable<Float> cir) {
		Zume.realFOV = cir.getReturnValueF();
		if (Zume.isActive()) {
			cir.setReturnValue((float) Zume.getFOV());
		}
	}
	
	@ModifyExpressionValue(method = "method_1844", at = @At(value = "FIELD", target = "Lnet/minecraft/client/options/GameOptions;cinematicMode:Z"))
	public boolean zume$updateMouse$smoothCameraEnabled(boolean original) {
		if (Zume.CONFIG.enableCinematicZoom && PrimitiveZume.INSTANCE.isZoomPressed()) {
			return true;
		}
		
		return original;
	}
	
	@ModifyExpressionValue(method = "method_1844", at = @At(value = "FIELD", target = "Lnet/minecraft/client/options/GameOptions;mouseSensitivity:F"))
	public float zume$updateMouse$mouseSensitivity(float original) {
		return (float) Zume.getMouseSensitivity(original);
	}
	
}
