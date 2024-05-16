package dev.nolij.zume.mixin.modern;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.nolij.zume.api.platform.v0.ZumeAPI;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;

@Mixin(value = MouseHandler.class, priority = 500)
public abstract class MouseHandlerMixin {
	
	@Dynamic
	@ModifyExpressionValue(method = {
		"turnPlayer", 
		"method_1606(D)V" // 20.5+ compat
	}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;smoothCamera:Z"))
	public boolean zume$updateMouse$smoothCameraEnabled(boolean original) {
		return ZumeAPI.cinematicCameraEnabledHook(original);
	}
	
	@SuppressWarnings("unchecked")
	@Dynamic
	@Group(name = "zume$getMouseSensitivity", min = 1, max = 1)
	@ModifyExpressionValue(method = {
		"turnPlayer", 
		"method_1606(D)V" // 20.5+ compat
	}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 0), require = 0)
	public <T> T zume$updateMouse$getMouseSensitivity$getValue(T original) {
		return (T) (Object) ZumeAPI.mouseSensitivityHook((Double) original);
	}
	
	@Dynamic
	@Group(name = "zume$getMouseSensitivity", min = 1, max = 1)
	@ModifyExpressionValue(method = "turnPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/class_315;field_1843:D", remap = false), require = 0)
	public double zume$updateMouse$mouseSensitivity(double original) {
		return ZumeAPI.mouseSensitivityHook(original);
	}
	
	@ModifyExpressionValue(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"))
	public boolean onMouseScroll$isSpectator(boolean original) {
		if (ZumeAPI.isMouseScrollHookActive())
			return false;
		
		return original;
	}
	
	@WrapWithCondition(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;swapPaint(D)V"))
	public boolean onMouseScroll$scrollInHotbar(Inventory instance, double scrollAmount) {
		return !ZumeAPI.mouseScrollHook((int) scrollAmount);
	}
	
}
