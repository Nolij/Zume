package dev.nolij.zume;

import dev.nolij.zume.common.Zume;
import dev.nolij.zume.common.ZumeVariant;
import dev.nolij.zume.modern.ModernZume;
import dev.nolij.zume.primitive.PrimitiveZume;
import dev.nolij.zume.legacy.LegacyZume;
import net.fabricmc.api.ClientModInitializer;

public class FabricZumeBootstrapper implements ClientModInitializer {
	
	@Override
	public void onInitializeClient() {
		switch (Zume.ZUME_VARIANT) {
			case ZumeVariant.MODERN -> new ModernZume().onInitializeClient();
			case ZumeVariant.LEGACY -> new LegacyZume().onInitializeClient();
			case ZumeVariant.PRIMITIVE -> new PrimitiveZume().onInitializeClient();
		}
	}
	
}
