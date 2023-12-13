package dev.nolij.zume.legacy.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.legacy.LegacyZume;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	
	@WrapWithCondition(method = {"tick", "method_12141()V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(I)V"))
	public boolean onMouseScroll$scrollInHotbar(PlayerInventory instance, int scrollAmount) {
		if (Zume.CONFIG.enableZoomScrolling)
			Zume.scrollDelta += scrollAmount > 0 ? 1 : -1;
		
		return !(Zume.CONFIG.enableZoomScrolling && LegacyZume.INSTANCE.isZoomPressed());
	}
	
}
