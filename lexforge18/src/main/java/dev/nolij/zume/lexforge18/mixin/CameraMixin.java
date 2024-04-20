package dev.nolij.zume.lexforge18.mixin;

import dev.nolij.zume.api.platform.v0.ZumeAPI;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = Camera.class, priority = 1500)
public class CameraMixin {
	
	@ModifyArg(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(D)D"))
	public double zume$setup$getMaxZoom(double original) {
        return ZumeAPI.thirdPersonCameraHook(original);
	}
	
}
