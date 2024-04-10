package dev.nolij.zume;

import dev.nolij.zume.common.Zume;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.impl.gui.FabricGuiEntry;
import net.fabricmc.loader.impl.gui.FabricStatusTree;

public class FabricZumePreLaunchEntrypoint implements PreLaunchEntrypoint {
	
	private static final String MISSING_DEPENDENCY_MESSAGE = """
		Failed to detect which variant of Zume to load! Ensure all dependencies are installed:
		    Fabric (14.4+): Fabric API (fabric-key-binding-api-v1)
		    Legacy Fabric (7.10-12.2): Legacy Fabric API (legacy-fabric-keybinding-api-v1-common)
		    Babric (b7.3): Station API (station-keybindings-v0)""";
	
	@Override
	public void onPreLaunch() {
		if (Zume.ZUME_VARIANT == null) {
			Zume.LOGGER.error(MISSING_DEPENDENCY_MESSAGE);
			FabricGuiEntry.displayError("Incompatible mods found!", null, tree -> {
				var tab = tree.addTab("Error");
				tab.node.addMessage(MISSING_DEPENDENCY_MESSAGE, FabricStatusTree.FabricTreeWarningLevel.ERROR);
				tree.tabs.removeIf(x -> x != tab);
			}, true);
		}
	}
	
}
