package dev.nolij.zume.mixin.modern;

import dev.nolij.zume.api.platform.v1.ZumeAPI;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = Camera.class, priority = 1500)
public class CameraMixin {
	
	@ModifyArg(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(D)D"))
	public double zume$update$clipToSpace(double original) {
        return ZumeAPI.thirdPersonCameraHook(original);
	}
	
}
