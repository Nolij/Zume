package dev.nolij.zume.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.nolij.zume.Zume;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import dev.nolij.zume.ZumeKeyBind;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	
	@WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(I)V"))
	public boolean onMouseScroll$scrollInHotbar(PlayerInventory instance, int scrollAmount) {
		if (Zume.CONFIG.enableZoomScrolling)
			Zume.scrollDelta += scrollAmount;
		
		return !(Zume.CONFIG.enableZoomScrolling && ZumeKeyBind.ZOOM.isPressed());
	}
	
}
