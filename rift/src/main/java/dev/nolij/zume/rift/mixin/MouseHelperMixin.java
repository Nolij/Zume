package dev.nolij.zume.rift.mixin;

import dev.nolij.zume.common.Zume;
import net.minecraft.client.GameSettings;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.InventoryPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHelper.class)
public class MouseHelperMixin {
	
	@Redirect(method = "updatePlayerLook", at = @At(value = "FIELD", target = "Lnet/minecraft/client/GameSettings;smoothCamera:Z"))
	public boolean zume$updateMouse$smoothCameraEnabled(GameSettings instance) {
		return Zume.transformCinematicCamera(instance.smoothCamera);
	}
	
	@Redirect(method = "updatePlayerLook", at = @At(value = "FIELD", target = "Lnet/minecraft/client/GameSettings;mouseSensitivity:D", ordinal = 0))
	public double zume$updateMouse$getMouseSensitivity$getValue(GameSettings instance) {
		return Zume.transformMouseSensitivity(instance.mouseSensitivity);
	}
	
	@Redirect(method = "scrollCallback", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isSpectator()Z"))
	public boolean scrollCallback$isSpectator(EntityPlayerSP instance) {
		if (Zume.CONFIG.enableZoomScrolling && Zume.ZUME_PROVIDER.isZoomPressed())
			return false;
		
		return instance.isSpectator();
	}
	
	@Redirect(method = "scrollCallback", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;changeCurrentItem(D)V"))
	public void scrollCallback$changeCurrentItem(InventoryPlayer instance, double scrollAmount) {
		if (Zume.transformHotbarScroll((int) scrollAmount)) {
			instance.changeCurrentItem(scrollAmount);
		}
	}
	
}
