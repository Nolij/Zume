package dev.nolij.zume.mixin.legacy;

import dev.nolij.zume.impl.Zume;
import dev.nolij.zume.legacy.ZumeKeyBind;
import net.legacyfabric.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameOptions.class, priority = 0)
public class GameOptionsMixin {
	
	@Unique
	private static boolean keybindsRegistered = false;
	
	@Inject(method = "load", at = @At("HEAD"))
	public void zume$load$HEAD(CallbackInfo ci) {
		if (Zume.disabled)
			return;
		
		if (!keybindsRegistered) {
			keybindsRegistered = true;
			
			for (final ZumeKeyBind keyBind : ZumeKeyBind.values()) {
				KeyBindingHelper.registerKeyBinding(keyBind.value);
			}
		}
	}
	
}
