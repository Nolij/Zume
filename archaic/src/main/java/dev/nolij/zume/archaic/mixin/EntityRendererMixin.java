package dev.nolij.zume.archaic.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.archaic.ArchaicZume;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

	@Dynamic
	@Inject(method = {
		"updateCameraAndRender", // archaic
		"func_181560_a(FJ)V" // vintage
	}, at = @At("HEAD"))
	public void zume$render$HEAD(CallbackInfo ci) {
		Zume.render();
	}
	
	@Inject(method = "getFOVModifier", at = @At("TAIL"), cancellable = true)
	public void zume$getFOV$TAIL(CallbackInfoReturnable<Float> cir) {
		Zume.realFOV = cir.getReturnValueF();
		if (Zume.isActive()) {
			cir.setReturnValue((float) Zume.getFOV());
		}
	}

	@Dynamic
	@ModifyExpressionValue(method = {
		"updateCameraAndRender", "updateRenderer", // archaic
		"func_181560_a(FJ)V", "func_78464_a()V" // vintage
	}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;smoothCamera:Z"))
	public boolean zume$updateMouse$smoothCameraEnabled(boolean original) {
		if (Zume.CONFIG.enableCinematicZoom && ArchaicZume.INSTANCE.isZoomPressed()) {
			return true;
		}

		return original;
	}

	@Dynamic
	@ModifyExpressionValue(method = {
		"updateCameraAndRender", "updateRenderer", // archaic
		"func_181560_a(FJ)V", "func_78464_a()V" // vintage
	}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;mouseSensitivity:F"))
	public float zume$updateMouse$mouseSensitivity(float original) {
		return (float) Zume.getMouseSensitivity(original);
	}

}
