package dev.nolij.zume.mixin.primitive;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.nolij.zume.api.platform.v0.ZumeAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Minecraft.class, priority = 500)
public abstract class MinecraftMixin {
	
	@WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;method_692(I)V"))
	public boolean onMouseScroll$scrollInHotbar(PlayerInventory instance, int scrollAmount) {
		return !ZumeAPI.mouseScrollHook(scrollAmount);
	}
	
}
