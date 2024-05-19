package dev.nolij.zume.mixin.lexforge;

import dev.nolij.zume.api.platform.v1.ZumeAPI;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	
	@Redirect(method = "turnPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;smoothCamera:Z"))
	public boolean zume$updateMouse$smoothCameraEnabled(Options instance) {
		return ZumeAPI.cinematicCameraEnabledHook(instance.smoothCamera);
	}
	
	@SuppressWarnings("unchecked")
	@Redirect(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 0))
	public <T> T zume$updateMouse$getMouseSensitivity$getValue(OptionInstance<Double> instance) {
		return (T) (Object) ZumeAPI.mouseSensitivityHook(instance.get());
	}
	
}
