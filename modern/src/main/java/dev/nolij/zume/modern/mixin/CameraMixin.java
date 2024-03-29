package dev.nolij.zume.modern.mixin;

import dev.nolij.zume.common.Zume;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = Camera.class, priority = 1500)
public class CameraMixin {
	
	@ModifyArg(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;clipToSpace(D)D"))
	public double zume$update$clipToSpace(double original) {
		if (Zume.shouldHook())
			return Zume.transformThirdPersonDistance(original);
		
		return original;
	}
	
}
