package dev.nolij.zume.mixin.modern;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.nolij.zume.impl.Zume;
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
		return Zume.cinematicCameraEnabledHook(original);
	}
	
	@SuppressWarnings("unchecked")
	@Dynamic
	@Group(name = "zume$getMouseSensitivity", min = 1, max = 1)
	@ModifyExpressionValue(method = {
		"turnPlayer", 
		"method_1606(D)V" // 20.5+ compat
	}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 0), require = 0)
	public <T> T zume$updateMouse$getMouseSensitivity$getValue(T original) {
		return (T) (Object) Zume.mouseSensitivityHook((Double) original);
	}
	
	@Dynamic
	@Group(name = "zume$getMouseSensitivity", min = 1, max = 1)
	@ModifyExpressionValue(method = "turnPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/class_315;field_1843:D", remap = false), require = 0)
	public double zume$updateMouse$mouseSensitivity(double original) {
		return Zume.mouseSensitivityHook(original);
	}
	
	@ModifyExpressionValue(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"))
	public boolean onMouseScroll$isSpectator(boolean original) {
		if (Zume.isMouseScrollHookActive())
			return false;
		
		return original;
	}
	
	@Group(name = "zume$onScroll", min = 1, max = 1)
	@WrapWithCondition(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;swapPaint(D)V"), require = 0)
	public boolean onMouseScroll$scrollInHotbar(Inventory instance, double scrollAmount) {
		return !Zume.mouseScrollHook((int) scrollAmount);
	}
	
	// 24w33a (21.2)+ compat
	@Dynamic
	@Group(name = "zume$onScroll", min = 1, max = 1)
	@WrapOperation(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_9928;method_61972(DII)I"), require = 0)
	public int zume$onScroll$getNextScrollWheelSelection(double scrollAmount, int current, int max, Operation<Integer> original) {
		if (Zume.mouseScrollHook((int) scrollAmount))
			return current;
		
		return original.call(scrollAmount, current, max);
	}
	
}
