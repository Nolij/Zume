package dev.nolij.zume.mixin.modern;

import dev.nolij.zume.api.platform.v0.ZumeAPI;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = Camera.class, priority = 1500)
public abstract class CameraMixin {
	
	@ModifyArg(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;clipToSpace(D)D"))
	public double zume$update$clipToSpace(double original) {
        return ZumeAPI.thirdPersonCameraHook(original);
	}
	
}
