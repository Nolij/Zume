package dev.nolij.zume.mixin.legacy;

import dev.nolij.zume.impl.Zume;
import dev.nolij.zumegradle.proguard.ProGuardKeep;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {
	
	@Shadow private boolean pressed;
	
	@SuppressWarnings({"MissingUnique", "unused", "MismatchedQueryAndUpdateOfCollection"})
	@ProGuardKeep
	private static Map<String, Integer> field_15867; // vintage intermediary
	
	@SuppressWarnings({"MissingUnique", "unused", "MismatchedQueryAndUpdateOfCollection"})
	@ProGuardKeep
	private static Set<String> field_7614; // archaic intermediary
	
	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void static$TAIL(CallbackInfo ci) {
		if (Zume.disabled)
			return;
		
		if (field_7614 != null)
			field_7614.add("zume");
		if (field_15867 != null)
			field_15867.put("zume", 8);
	}
	
	// ugly hack for <=6.4 compat
	@SuppressWarnings({"MissingUnique", "unused"})
	@ProGuardKeep
	public boolean method_6619() {
		return this.pressed;
	}
	
}
