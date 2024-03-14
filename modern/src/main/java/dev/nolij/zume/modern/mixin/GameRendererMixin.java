package dev.nolij.zume.modern.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.nolij.zume.common.Zume;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	
	@Inject(method = "render", at = @At("HEAD"))
	public void zume$render$HEAD(CallbackInfo ci) {
		Zume.render();
	}
	
	@ModifyReturnValue(method = "getFov", at = @At("TAIL"))
	public double zume$getFov$TAIL(double original) {
		if (Zume.shouldHookFOV())
			return Zume.transformFOV(original);
		
		return original;
	}
	
}
