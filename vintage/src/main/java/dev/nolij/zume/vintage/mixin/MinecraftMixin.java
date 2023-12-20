package dev.nolij.zume.vintage.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.nolij.zume.common.Zume;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public class MinecraftMixin {

	@WrapWithCondition(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;changeCurrentItem(I)V"))
	public boolean onMouseScroll$scrollInHotbar(InventoryPlayer instance, int scrollAmount) {
		return Zume.transformHotbarScroll(scrollAmount);
	}

}
