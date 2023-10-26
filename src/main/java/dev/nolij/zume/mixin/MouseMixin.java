package dev.nolij.zume.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.nolij.zume.Zume;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import dev.nolij.zume.ZumeKeyBind;

@Mixin(Mouse.class)
public class MouseMixin {
	
	@ModifyExpressionValue(method = "updateMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;smoothCameraEnabled:Z"))
	public boolean zume$updateMouse$smoothCameraEnabled(boolean original) {
		if (Zume.CONFIG.enableCinematicZoom && ZumeKeyBind.ZOOM.isPressed()) {
			return true;
		}
		
		return original;
	}
	
	@SuppressWarnings("unchecked")
	@ModifyExpressionValue(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;", ordinal = 0), require = 0)
	public <T> T zume$updateMouse$getMouseSensitivity$getValue(T original) {
		if (!Zume.CONFIG.enableCinematicZoom && ZumeKeyBind.ZOOM.isPressed()) {
			return (T) (Object) (((Double) original) * Zume.CONFIG.mouseSensitivityMultiplier);
		}
		
		return original;
	}
	
	@SuppressWarnings("MixinAnnotationTarget")
	@ModifyExpressionValue(method = "updateMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/class_315;field_1843:D", remap = false), require = 0)
	public double zume$updateMouse$mouseSensitivity(double original) {
		if (!Zume.CONFIG.enableCinematicZoom && ZumeKeyBind.ZOOM.isPressed()) {
			return original * Zume.CONFIG.mouseSensitivityMultiplier;
		}
		
		return original;
	}
	
}
