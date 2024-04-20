package dev.nolij.zume.legacy.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.nolij.zume.api.platform.v0.ZumeAPI;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	
	@Dynamic
	@Inject(method = {
		"method_1331", // archaic
		"method_9775(FJ)V" // vintage
	}, at = @At("HEAD"))
	public void zume$render$HEAD(CallbackInfo ci) {
		ZumeAPI.renderHook();
	}
	
	@ModifyReturnValue(method = "getFov", at = @At("TAIL"))
	public float zume$getFOV$TAIL(float original) {
		if (ZumeAPI.isFOVHookActive())
			return (float) ZumeAPI.fovHook(original);
		
		return original;
	}
	
	@Dynamic
	@ModifyExpressionValue(method = {
		"method_1331", "tick", // archaic
		"method_9775(FJ)V" // vintage
	}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;smoothCameraEnabled:Z"))
	public boolean zume$smoothCameraEnabled(boolean original) {
		return ZumeAPI.cinematicCameraEnabledHook(original);
	}
	
	@Dynamic
	@ModifyExpressionValue(method = {
		"method_1331", "tick", // archaic
		"method_9775(FJ)V" // vintage
	}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;sensitivity:F"))
	public float zume$mouseSensitivity(float original) {
		return (float) ZumeAPI.mouseSensitivityHook(original);
	}
	
	@ModifyVariable(method = "transformCamera", at = @At(value = "STORE", ordinal = 0), ordinal = 3)
	public double zume$transformCamera$thirdPersonDistance(double original) {
        return ZumeAPI.thirdPersonCameraHook(original);
	}
	
}
