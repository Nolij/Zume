package dev.nolij.zume;

import dev.nolij.zume.common.Zume;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.impl.gui.FabricGuiEntry;
import net.fabricmc.loader.impl.gui.FabricStatusTree;

public class FabricZumePreLaunchEntrypoint implements PreLaunchEntrypoint {
	
	@Override
	public void onPreLaunch() {
		if (Zume.ZUME_VARIANT == null) {
			FabricGuiEntry.displayError("Incompatible mods found!", null, tree -> {
				var tab = tree.addTab("Error");
				tab.node.addMessage("""
				Failed to detect which variant of Zume to load! Ensure all dependencies are installed:
				Fabric (14.4+): fabric-key-binding-api-v1
				Legacy Fabric (7.10-12.2): legacy-fabric-keybinding-api-v1-common
				Babric (b7.3): station-keybindings-v0""", FabricStatusTree.FabricTreeWarningLevel.ERROR);
				tree.tabs.removeIf(x -> x != tab);
			}, true);
		}
	}
	
}
