package dev.nolij.zume.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.nolij.zume.Zume;
import dev.nolij.zume.ZumeKeyBind;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	
	@Shadow private boolean renderingPanorama;
	
	@Inject(method = "getFov", at = @At("HEAD"), cancellable = true)
	public void zume$getFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
		if (!this.renderingPanorama && ZumeKeyBind.ZOOM.isPressed()) {
			if (ZumeKeyBind.ZOOM.wasPressed())
				Zume.zoom = Zume.defaultZoom;
			
			cir.setReturnValue(Zume.getFOV());
		}
	}
	
}
