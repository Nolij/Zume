package dev.nolij.zume.archaic.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.nolij.zume.common.Zume;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

	@Inject(method = "updateCameraAndRender", at = @At("HEAD"))
	public void zume$render$HEAD(CallbackInfo ci) {
		Zume.render();
	}
	
	@ModifyReturnValue(method = "getFOVModifier", at = @At("TAIL"))
	public float zume$getFOV$TAIL(float original) {
		if (Zume.shouldHookFOV())
			return (float) Zume.transformFOV(original);
		
		return original;
	}

	@ModifyExpressionValue(method = {"updateCameraAndRender", "updateRenderer"}, 
		at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;smoothCamera:Z"))
	public boolean zume$updateMouse$smoothCameraEnabled(boolean original) {
		return Zume.transformCinematicCamera(original);
	}

	@ModifyExpressionValue(method = {"updateCameraAndRender", "updateRenderer"}, 
		at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;mouseSensitivity:F"))
	public float zume$updateMouse$mouseSensitivity(float original) {
		return (float) Zume.transformMouseSensitivity(original);
	}
	
	@ModifyExpressionValue(method = "orientCamera", 
		at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;thirdPersonDistance:F"))
	public float zume$orientCamera$thirdPersonDistance(float original) {
		if (Zume.shouldHook())
			return (float) Zume.transformThirdPersonDistance(original);
		
		return original;
	}
	
	@ModifyExpressionValue(method = "orientCamera", 
		at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;thirdPersonDistanceTemp:F"))
	public float zume$orientCamera$thirdPersonDistanceTemp(float original) {
		if (Zume.shouldHook())
			return (float) Zume.transformThirdPersonDistance(original);
		
		return original;
	}

}
