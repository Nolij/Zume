package dev.nolij.zume.mixin.lexforge16;

import dev.nolij.zume.api.platform.v1.ZumeAPI;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	
	@Redirect(method = "turnPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;smoothCamera:Z"))
	public boolean zume$updateMouse$smoothCameraEnabled(Options instance) {
		return ZumeAPI.cinematicCameraEnabledHook(instance.smoothCamera);
	}
	
	@Redirect(method = "turnPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;sensitivity:D", ordinal = 0))
	public double zume$updateMouse$getMouseSensitivity$getValue(Options instance) {
		return ZumeAPI.mouseSensitivityHook(instance.sensitivity);
	}
	
}
