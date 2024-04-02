package dev.nolij.zume.primitive.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.nolij.zume.common.Zume;
import net.minecraft.class_555;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_555.class)
public class GameRendererMixin {
	
	@Inject(method = "method_1844", at = @At("HEAD"))
	public void zume$render$HEAD(CallbackInfo ci) {
		Zume.render();
	}
	
	@ModifyReturnValue(method = "method_1848", at = @At("TAIL"))
	public float zume$getFov$TAIL(float original) {
		if (Zume.shouldHookFOV())
			return (float) Zume.transformFOV(original);
		
		return original;
	}
	
	@ModifyExpressionValue(method = "method_1844", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;cinematicMode:Z"))
	public boolean zume$updateMouse$smoothCameraEnabled(boolean original) {
		return Zume.transformCinematicCamera(original);
	}
	
	@ModifyExpressionValue(method = "method_1844", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;mouseSensitivity:F"))
	public float zume$updateMouse$mouseSensitivity(float original) {
		return (float) Zume.transformMouseSensitivity(original);
	}
	
	@ModifyExpressionValue(method = "method_1851", at = @At(value = "FIELD", target = "Lnet/minecraft/class_555;field_2359:F"))
	public float zume$transformCamera$thirdPersonDistance(float original) {
        return (float) Zume.transformThirdPersonDistance(original);
	}
	
	@ModifyExpressionValue(method = "method_1851", at = @At(value = "FIELD", target = "Lnet/minecraft/class_555;field_2360:F"))
	public float zume$transformCamera$lastThirdPersonDistance(float original) {
        return (float) Zume.transformThirdPersonDistance(original);
	}
	
}
