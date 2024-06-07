package dev.nolij.zume.mixin.modern;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.nolij.zume.impl.Zume;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	
	@Inject(method = "render", at = @At("HEAD"))
	public void zume$render$HEAD(CallbackInfo ci) {
		Zume.renderHook();
	}
	
	@ModifyReturnValue(method = "getFov", at = @At("TAIL"))
	public double zume$getFov$TAIL(double original) {
		if (Zume.isFOVHookActive())
			return Zume.fovHook(original);
		
		return original;
	}
	
}
