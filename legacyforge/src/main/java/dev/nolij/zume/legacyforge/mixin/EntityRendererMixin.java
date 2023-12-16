package dev.nolij.zume.legacyforge.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.nolij.zume.common.Zume;
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
		"func_78480_b(F)V", // archaic - updateCameraAndRender
		"func_181560_a(FJ)V" // vintage
	}, remap = false, at = @At("HEAD"))
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
		"func_78480_b(F)V", "func_78464_a()V", // archaic - updateCameraAndRender, updateRenderer
		"func_181560_a(FJ)V", "func_78464_a()V" // vintage
	}, remap = false, at = @At(value = "FIELD",
		target = "Lnet/minecraft/client/settings/GameSettings;field_74326_T:Z"), require = 0)
	public boolean zume$updateMouse$smoothCameraEnabled(boolean original) {
		if (Zume.CONFIG.enableCinematicZoom && Zume.ZUME_PROVIDER.isZoomPressed()) {
			return true;
		}

		return original;
	}

	@Dynamic
	@ModifyExpressionValue(method = {
		"func_78480_b(F)V", "func_78464_a()V", // archaic - updateCameraAndRender, updateRenderer
		"func_181560_a(FJ)V", "func_78464_a()V" // vintage
	}, remap = false, at = @At(value = "FIELD", 
		target = "Lnet/minecraft/client/settings/GameSettings;field_74341_c:F"), require = 0)
	public float zume$updateMouse$mouseSensitivity(float original) {
		return (float) Zume.getMouseSensitivity(original);
	}

}
