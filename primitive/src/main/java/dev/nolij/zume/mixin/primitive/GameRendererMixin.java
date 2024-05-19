package dev.nolij.zume.mixin.primitive;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.nolij.zume.api.platform.v1.ZumeAPI;
import net.minecraft.class_555;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_555.class)
public abstract class GameRendererMixin {
	
	@Inject(method = "method_1844", at = @At("HEAD"))
	public void zume$render$HEAD(CallbackInfo ci) {
		ZumeAPI.renderHook();
	}
	
	@ModifyReturnValue(method = "method_1848", at = @At("TAIL"))
	public float zume$getFov$TAIL(float original) {
		if (ZumeAPI.isFOVHookActive())
			return (float) ZumeAPI.fovHook(original);
		
		return original;
	}
	
	@ModifyExpressionValue(method = "method_1844", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;cinematicMode:Z"))
	public boolean zume$updateMouse$smoothCameraEnabled(boolean original) {
		return ZumeAPI.cinematicCameraEnabledHook(original);
	}
	
	@ModifyExpressionValue(method = "method_1844", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;mouseSensitivity:F"))
	public float zume$updateMouse$mouseSensitivity(float original) {
		return (float) ZumeAPI.mouseSensitivityHook(original);
	}
	
	@ModifyExpressionValue(method = "method_1851", at = @At(value = "FIELD", target = "Lnet/minecraft/class_555;field_2359:F"))
	public float zume$transformCamera$thirdPersonDistance(float original) {
        return (float) ZumeAPI.thirdPersonCameraHook(original);
	}
	
	@ModifyExpressionValue(method = "method_1851", at = @At(value = "FIELD", target = "Lnet/minecraft/class_555;field_2360:F"))
	public float zume$transformCamera$lastThirdPersonDistance(float original) {
        return (float) ZumeAPI.thirdPersonCameraHook(original);
	}
	
}
