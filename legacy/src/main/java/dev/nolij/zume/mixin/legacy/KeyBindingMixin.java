package dev.nolij.zume.mixin.legacy;

import dev.nolij.zumegradle.proguard.ProGuardKeep;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {
	
	@Shadow private boolean pressed;
	
	// ugly hack for <=6.4 compat
	@SuppressWarnings({"MissingUnique", "unused"})
	@ProGuardKeep
	public boolean method_6619() {
		return this.pressed;
	}
	
}
