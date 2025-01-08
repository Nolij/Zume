package dev.nolij.zume.mixin.primitive;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.nolij.zume.impl.Zume;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Minecraft.class, priority = 500)
public abstract class MinecraftMixin {
	
	@WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;swapPaint(I)V"))
	public boolean onMouseScroll$scrollInHotbar(Inventory instance, int scrollAmount) {
		return !Zume.mouseScrollHook(scrollAmount);
	}
	
}
