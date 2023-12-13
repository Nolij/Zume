package dev.nolij.zume.primitive.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.primitive.PrimitiveZume;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	
	@WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(I)V"))
	public boolean onMouseScroll$scrollInHotbar(PlayerInventory instance, int scrollAmount) {
		if (Zume.CONFIG.enableZoomScrolling)
			Zume.scrollDelta += scrollAmount > 0 ? 1 : -1;
		
		return !(Zume.CONFIG.enableZoomScrolling && PrimitiveZume.INSTANCE.isZoomPressed());
	}
	
}
