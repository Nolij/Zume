package dev.nolij.zume.neoforge.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.neoforge.NeoZume;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	
	@ModifyExpressionValue(method = "turnPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;smoothCamera:Z"))
	public boolean zume$updateMouse$smoothCameraEnabled(boolean original) {
		return Zume.transformCinematicCamera(original);
	}
	
	@SuppressWarnings("unchecked")
	@ModifyExpressionValue(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 0))
	public <T> T zume$updateMouse$getMouseSensitivity$getValue(T original) {
		return (T) (Object) Zume.transformMouseSensitivity((Double) original);
	}
	
}
