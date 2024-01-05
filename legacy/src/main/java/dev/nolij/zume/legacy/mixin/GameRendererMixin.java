package dev.nolij.zume.legacy.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.legacy.LegacyZume;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	
	@Dynamic
	@Inject(method = {
		"method_1331", // archaic
		"method_9775(FJ)V" // vintage
	}, at = @At("HEAD"))
	public void zume$render$HEAD(CallbackInfo ci) {
		Zume.render();
	}
	
	@Inject(method = "getFov", at = @At("TAIL"), cancellable = true)
	public void zume$getFOV$TAIL(CallbackInfoReturnable<Float> cir) {
		if (Zume.isActive()) {
			cir.setReturnValue((float) Zume.transformFOV(cir.getReturnValueF()));
		}
	}
	
	@Dynamic
	@ModifyExpressionValue(method = {
		"method_1331", "tick", // archaic
		"method_9775(FJ)V" // vintage
	}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;smoothCameraEnabled:Z"))
	public boolean zume$smoothCameraEnabled(boolean original) {
		return Zume.transformCinematicCamera(original);
	}
	
	@Dynamic
	@ModifyExpressionValue(method = {
		"method_1331", "tick", // archaic
		"method_9775(FJ)V" // vintage
	}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;sensitivity:F"))
	public float zume$mouseSensitivity(float original) {
		return (float) Zume.transformMouseSensitivity(original);
	}
	
}
