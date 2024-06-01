package dev.nolij.zume;

import dev.nolij.zume.api.platform.v1.ZumeAPI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.impl.gui.FabricGuiEntry;
import net.fabricmc.loader.impl.gui.FabricStatusTree;

public class FabricZumeBootstrapper implements ClientModInitializer, PreLaunchEntrypoint {
	
	private static final String MISSING_DEPENDENCY_MESSAGE = """
		Failed to detect which variant of Zume to load! Ensure all dependencies are installed:
		    Fabric (14.4+): Fabric API (fabric-key-binding-api-v1)
		    Legacy Fabric (7.10-12.2): Legacy Fabric API (legacy-fabric-keybinding-api-v1-common)
		    Babric (b7.3): Station API (station-keybindings-v0)""";
	
	@Override
	public void onPreLaunch() {
		if (ZumeMixinPlugin.ZUME_VARIANT != null)
			return;
		
		ZumeAPI.getLogger().error(MISSING_DEPENDENCY_MESSAGE);
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
		
		String className = switch (ZumeMixinPlugin.ZUME_VARIANT) {
			case ZumeVariant.MODERN -> "dev.nolij.zume.modern.ModernZume";
			case ZumeVariant.LEGACY -> "dev.nolij.zume.legacy.LegacyZume";
			case ZumeVariant.PRIMITIVE -> "dev.nolij.zume.primitive.PrimitiveZume";
			default -> "[unknown variant]";
		};
		try {
			((ClientModInitializer)Class.forName(className).getConstructor().newInstance()).onInitializeClient();
		} catch(ReflectiveOperationException e) {
			throw null; // Save some bytecode by not throwing a real exception
		}
	}
	
}
