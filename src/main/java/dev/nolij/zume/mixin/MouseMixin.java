package dev.nolij.zume.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import dev.nolij.zume.ZumeKeyBind;

@Mixin(Mouse.class)
public class MouseMixin {
	
	@ModifyExpressionValue(method = "updateMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;smoothCameraEnabled:Z"))
	public boolean zume$updateMouse$smoothCameraEnabled(boolean original) {
		if (ZumeKeyBind.ZOOM.isPressed()) {
			return true;
		}
		
		return original;
	}
	
}
