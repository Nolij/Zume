package dev.nolij.zume;

import dev.nolij.zume.impl.Zume;
import dev.nolij.zume.legacy.LegacyZume;
import dev.nolij.zume.modern.ModernZume;
import dev.nolij.zume.primitive.PrimitiveZume;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.impl.gui.FabricGuiEntry;
import net.fabricmc.loader.impl.gui.FabricStatusTree;

public class FabricZumeBootstrapper implements ClientModInitializer, PreLaunchEntrypoint {
	
	private static final String MISSING_DEPENDENCY_MESSAGE = """
		Failed to detect which variant of Zume to load! Ensure all dependencies are installed:
		    Fabric (14.4+): Fabric API (fabric-key-binding-api-v1)
		    Legacy Fabric (6.4-12.2): Legacy Fabric API (legacy-fabric-keybinding-api-v1-common)
		    Babric (b7.3): Station API (station-keybindings-v0)""";
	
	@Override
	public void onPreLaunch() {
		if (ZumeMixinPlugin.ZUME_VARIANT != null)
			return;
		
		Zume.LOGGER.error(MISSING_DEPENDENCY_MESSAGE);
		FabricGuiEntry.displayError("Incompatible mods found!", null, tree -> {
			var tab = tree.addTab("Error");
			tab.node.addMessage(MISSING_DEPENDENCY_MESSAGE, FabricStatusTree.FabricTreeWarningLevel.ERROR);
			tree.tabs.removeIf(x -> x != tab);
		}, true);
	}
	
	@Override
	public void onInitializeClient() {
		if (ZumeMixinPlugin.ZUME_VARIANT == null)
			return;
		
		switch (ZumeMixinPlugin.ZUME_VARIANT) {
			case ZumeMixinPlugin.MODERN -> new ModernZume().onInitializeClient();
			case ZumeMixinPlugin.LEGACY -> new LegacyZume().onInitializeClient();
			case ZumeMixinPlugin.PRIMITIVE -> new PrimitiveZume().onInitializeClient();
		}
		
		Zume.postInit();
	}
	
}
