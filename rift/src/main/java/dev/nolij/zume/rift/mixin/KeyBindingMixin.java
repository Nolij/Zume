package dev.nolij.zume.rift.mixin;

import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(KeyBinding.class)
public class KeyBindingMixin {
	
	@Inject(method = "func_205215_a", at = @At("TAIL"))
	private static void static$CATEGORY_ORDER$lambda(HashMap<String, Integer> hashMap, CallbackInfo ci) {
		hashMap.put("category.zume", 8);
	}
	
}
