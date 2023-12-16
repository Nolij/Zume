package dev.nolij.zume.legacyforge;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LegacyForgeZumeBootstrapper implements IEarlyMixinLoader, IFMLLoadingPlugin {
	
	@Override
	public String getMixinConfig() {
		return "zume-legacyforge.mixins.json";
	}
	
	@Override
	public List<String> getMixins(Set<String> loadedCoreMods) {
		return Collections.emptyList();
	}
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[0];
	}
	
	@Override
	public String getModContainerClass() {
		return null;
	}
	
	@Override
	public String getSetupClass() {
		return null;
	}
	
	@Override
	public void injectData(Map<String, Object> data) {
		
	}
	
	@Override
	public String getAccessTransformerClass() {
		return null;
	}
	
}
