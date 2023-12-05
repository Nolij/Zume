package dev.nolij.zume.event;

import dev.nolij.zume.ZumeKeyBind;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.client.option.KeyBinding;
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
