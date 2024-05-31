package dev.nolij.zume.mixin.vintage;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.nolij.zume.api.platform.v1.ZumeAPI;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
	
	@ModifyExpressionValue(method = { "updateCameraAndRender", "updateRenderer" },
		at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;smoothCamera:Z"))
	public boolean zume$updateMouse$smoothCameraEnabled(boolean original) {
		return ZumeAPI.cinematicCameraEnabledHook(original);
	}
	
	@ModifyExpressionValue(method = { "updateCameraAndRender", "updateRenderer" },
		at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;mouseSensitivity:F"))
	public float zume$updateMouse$mouseSensitivity(float original) {
		return (float) ZumeAPI.mouseSensitivityHook(original);
	}
	
	@ModifyVariable(method = "orientCamera", at = @At(value = "STORE", ordinal = 0), ordinal = 3)
	public double zume$orientCamera$thirdPersonDistance(double original) {
		return ZumeAPI.thirdPersonCameraHook(original);
	}
	
}
