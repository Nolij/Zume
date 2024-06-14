package dev.nolij.zume.mixin.legacy;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.nolij.zume.impl.Zume;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	
	@Dynamic
	@Inject(method = {
		"method_1331", // archaic
		"method_9775(FJ)V" // vintage
	}, at = @At("HEAD"))
	public void zume$render$HEAD(CallbackInfo ci) {
		Zume.renderHook();
	}
	
	@ModifyReturnValue(method = "getFov", at = @At("TAIL"))
	public float zume$getFOV$TAIL(float original) {
		if (Zume.isFOVHookActive())
			return (float) Zume.fovHook(original);
		
		return original;
	}
	
	@Dynamic
	@ModifyExpressionValue(method = {
		"method_1331", "tick", // archaic
		"method_9775(FJ)V" // vintage
	}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;smoothCameraEnabled:Z"))
	public boolean zume$smoothCameraEnabled(boolean original) {
		return Zume.cinematicCameraEnabledHook(original);
	}
	
	@Dynamic
	@ModifyExpressionValue(method = {
		"method_1331", "tick", // archaic
		"method_9775(FJ)V" // vintage
	}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;sensitivity:F"))
	public float zume$mouseSensitivity(float original) {
		return (float) Zume.mouseSensitivityHook(original);
	}
	
	@ModifyVariable(method = "transformCamera", at = @At(value = "STORE", ordinal = 0), ordinal = 3)
	public double zume$transformCamera$thirdPersonDistance(double original) {
        return Zume.thirdPersonCameraHook(original);
	}
	
}
