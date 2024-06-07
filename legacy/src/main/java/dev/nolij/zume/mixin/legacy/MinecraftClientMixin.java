package dev.nolij.zume.mixin.legacy;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.nolij.zume.impl.Zume;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = MinecraftClient.class, priority = 500)
public class MinecraftClientMixin {
	
	@Dynamic
	@WrapWithCondition(method = {
		"tick", // archaic 
		"method_12141()V" // vintage
	}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(I)V"))
	public boolean onMouseScroll$scrollInHotbar(PlayerInventory instance, int scrollAmount) {
		return !Zume.mouseScrollHook(scrollAmount);
	}
	
}
