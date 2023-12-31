package dev.nolij.zume.primitive.event;

import dev.nolij.zume.primitive.ZumeKeyBind;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.client.options.KeyBinding;
import net.modificationstation.stationapi.api.client.event.option.KeyBindingRegisterEvent;

import java.util.List;

public class KeyBindingRegistrar {
	
	@EventListener
	public void registerKeyBindings(KeyBindingRegisterEvent event) {
		final List<KeyBinding> binds = event.keyBindings;
		
		for (final ZumeKeyBind keyBind : ZumeKeyBind.values()) {
			binds.add(keyBind.value);
		}
	}
	
}
