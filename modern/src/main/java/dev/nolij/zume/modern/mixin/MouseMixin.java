package dev.nolij.zume.modern.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.modern.ModernZume;
import net.minecraft.client.Mouse;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mouse.class)
public class MouseMixin {
	
	@Dynamic
	@ModifyExpressionValue(method = {
		"updateMouse", 
		"method_1606(D)V" // 20.5+ compat
	}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;smoothCameraEnabled:Z"))
	public boolean zume$updateMouse$smoothCameraEnabled(boolean original) {
		return Zume.transformCinematicCamera(original);
	}
	
	@SuppressWarnings("unchecked")
	@Dynamic
	@ModifyExpressionValue(method = {
		"updateMouse", 
		"method_1606(D)V" // 20.5+ compat
	}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;", ordinal = 0), require = 0)
	public <T> T zume$updateMouse$getMouseSensitivity$getValue(T original) {
		return (T) (Object) Zume.transformMouseSensitivity((Double) original);
	}
	
	@Dynamic
	@ModifyExpressionValue(method = "updateMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/class_315;field_1843:D", remap = false), require = 0)
	public double zume$updateMouse$mouseSensitivity(double original) {
		return Zume.transformMouseSensitivity(original);
	}
	
	@ModifyExpressionValue(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSpectator()Z"))
	public boolean onMouseScroll$isSpectator(boolean original) {
		if (Zume.CONFIG.enableZoomScrolling && Zume.isActive())
			return false;
		
		return original;
	}
	
	@WrapWithCondition(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"))
	public boolean onMouseScroll$scrollInHotbar(PlayerInventory instance, double scrollAmount) {
		return Zume.transformHotbarScroll((int) scrollAmount);
	}
	
}
