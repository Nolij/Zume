package dev.nolij.zume;

import dev.nolij.zume.common.Zume;
import dev.nolij.zume.modern.ModernZume;
import dev.nolij.zume.primitive.PrimitiveZume;
import dev.nolij.zume.legacy.LegacyZume;
import net.fabricmc.api.ClientModInitializer;

public class FabricZumeBootstrapper implements ClientModInitializer {
	
	@Override
	public void onInitializeClient() {
		if (Zume.ZUME_VARIANT == null)
			throw new AssertionError("""
				Failed to detect which variant of Zume to load!
				Ensure all dependencies are installed:
				Fabric (14.4+): fabric-key-binding-api-v1
				Legacy Fabric (7.10-12.2): legacy-fabric-keybinding-api-v1-common
				Babric (b7.3): station-keybindings-v0""");
		
		switch (Zume.ZUME_VARIANT) {
			case MODERN -> new ModernZume().onInitializeClient();
			case LEGACY -> new LegacyZume().onInitializeClient();
			case PRIMITIVE -> new PrimitiveZume().onInitializeClient();
		}
	}
	
}
