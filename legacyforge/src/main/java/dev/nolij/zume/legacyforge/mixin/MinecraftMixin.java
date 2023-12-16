package dev.nolij.zume.legacyforge.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.nolij.zume.common.Zume;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public class MinecraftMixin {

	@Dynamic
	@WrapWithCondition(method = {
		"func_71407_l()V", // archaic - runTick 
		"func_184124_aB()V" // vintage
	}, remap = false, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;func_70453_c(I)V"), require = 0)
	public boolean onMouseScroll$scrollInHotbar(InventoryPlayer instance, int scrollAmount) {
		if (Zume.CONFIG.enableZoomScrolling)
			Zume.scrollDelta += scrollAmount > 0 ? 1 : -1;

		return !(Zume.CONFIG.enableZoomScrolling && Zume.ZUME_PROVIDER.isZoomPressed());
	}

}
