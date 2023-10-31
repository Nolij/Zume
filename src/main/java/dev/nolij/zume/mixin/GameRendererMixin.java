package dev.nolij.zume.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.nolij.zume.Zume;
import dev.nolij.zume.ZumeKeyBind;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	
	@Shadow public int field_1859;
	
	@Inject(method = "method_1331", at = @At("HEAD"))
	public void zume$render$HEAD(CallbackInfo ci) {
		Zume.render();
	}
	
	@Inject(method = "getFov", at = @At("HEAD"), cancellable = true)
	public void zume$getFov$HEAD(CallbackInfoReturnable<Float> cir) {
		if (this.field_1859 <= 0 && ZumeKeyBind.ZOOM.isPressed()) {			
			cir.setReturnValue((float) Zume.getFOV());
		}
	}
	
	@ModifyExpressionValue(method = {"tick", "method_1331"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;smoothCameraEnabled:Z"))
	public boolean zume$tick$smoothCameraEnabled(boolean original) {
		if (Zume.CONFIG.enableCinematicZoom && ZumeKeyBind.ZOOM.isPressed()) {
			return true;
		}
		
		return original;
	}
	
	@ModifyExpressionValue(method = {"tick", "method_1331"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;sensitivity:F"))
	public float zume$tick$mouseSensitivity(float original) {
		if (!Zume.CONFIG.enableCinematicZoom && ZumeKeyBind.ZOOM.isPressed()) {
			return original * (float) Zume.CONFIG.mouseSensitivityMultiplier;
		}
		
		return original;
	}
	
}
