package dev.nolij.zume.mixin.primitive;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.nolij.zume.impl.Zume;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	
	@Inject(method = "render(F)V", at = @At("HEAD"))
	public void zume$render$HEAD(CallbackInfo ci) {
		Zume.renderHook();
	}
	
	@ModifyReturnValue(method = "getFov", at = @At("TAIL"))
	public float zume$getFov$TAIL(float original) {
		if (Zume.isFOVHookActive())
			return (float) Zume.fovHook(original);
		
		return original;
	}
	
	@ModifyExpressionValue(method = "render(F)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;smoothCamera:Z"))
	public boolean zume$updateMouse$smoothCameraEnabled(boolean original) {
		return Zume.cinematicCameraEnabledHook(original);
	}
	
	@ModifyExpressionValue(method = "render(F)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;mouseSensitivity:F"))
	public float zume$updateMouse$mouseSensitivity(float original) {
		return (float) Zume.mouseSensitivityHook(original);
	}
	
	@ModifyExpressionValue(method = "method_1851", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;field_2359:F"))
	public float zume$transformCamera$thirdPersonDistance(float original) {
        return (float) Zume.thirdPersonCameraHook(original);
	}
	
	@ModifyExpressionValue(method = "method_1851", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;field_2360:F"))
	public float zume$transformCamera$lastThirdPersonDistance(float original) {
        return (float) Zume.thirdPersonCameraHook(original);
	}
	
}
