package dev.nolij.zume.lexforge.mixin;

import dev.nolij.zume.common.Zume;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = Camera.class, priority = 1500)
public class CameraMixin {
	
	@ModifyArg(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(D)D"))
	public double zume$setup$getMaxZoom(double original) {
		if (Zume.shouldHook())
			return Zume.transformThirdPersonDistance(original);
		
		return original;
	}
	
}
