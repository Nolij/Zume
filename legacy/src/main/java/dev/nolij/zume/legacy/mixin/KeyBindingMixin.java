package dev.nolij.zume.legacy.mixin;

import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;

@Mixin(KeyBinding.class)
public class KeyBindingMixin {
	
	@SuppressWarnings({"MissingUnique", "unused", "MismatchedQueryAndUpdateOfCollection"})	
	private static Map<String, Integer> field_15867; // vintage intermediary
	
	@Shadow @Final private static Set<String> categories;
	
	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void static$TAIL(CallbackInfo ci) {
		categories.add("category.zume");
		if (field_15867 != null)
			field_15867.put("category.zume", 8);
	}
	
}
