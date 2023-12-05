package dev.nolij.zume.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.nolij.zume.Zume;
import net.minecraft.class_555;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import dev.nolij.zume.ZumeKeyBind;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_555.class)
public class class_555Mixin {
	
	@Inject(method = "method_1844", at = @At("HEAD"))
	public void zume$render$HEAD(CallbackInfo ci) {
		Zume.render();
	}
	
	@Inject(method = "method_1848", at = @At("TAIL"), cancellable = true)
	public void zume$getFov$TAIL(CallbackInfoReturnable<Float> cir) {
		Zume.realFOV = cir.getReturnValueF();
		if (Zume.isActive()) {
			cir.setReturnValue(Zume.getFOV());
		}
	}
	
	@ModifyExpressionValue(method = "method_1844", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;cinematicMode:Z"))
	public boolean zume$updateMouse$smoothCameraEnabled(boolean original) {
		if (Zume.CONFIG.enableCinematicZoom && ZumeKeyBind.ZOOM.isPressed()) {
			return true;
		}
		
		return original;
	}
	
	@ModifyExpressionValue(method = "method_1844", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;mouseSensitivity:F"))
	public float zume$updateMouse$mouseSensitivity(float original) {
		return Zume.getMouseSensitivity(original);
	}
	
}
