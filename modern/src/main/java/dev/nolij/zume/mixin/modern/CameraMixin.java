package dev.nolij.zume.mixin.modern;

import dev.nolij.zume.impl.Zume;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = Camera.class, priority = 1500)
public abstract class CameraMixin {
	
	@Group(name = "zume$thirdPersonCameraHook", min = 1, max = 1)
	@ModifyArg(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(D)D"), require = 0)
	public double zume$update$clipToSpace(double original) {
        return Zume.thirdPersonCameraHook(original);
	}
	
	@Dynamic
	@Group(name = "zume$thirdPersonCameraHook", min = 1, max = 1)
	@ModifyArg(method = "method_19321*", remap = false, at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4184;method_19318(F)F", remap = false), require = 0)
	public float zume$update$clipToSpace(float original) {
        return (float) Zume.thirdPersonCameraHook(original);
	}
	
}
