package dev.nolij.zume.legacyforge;

import dev.nolij.zume.common.Zume;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Mod(
	modid = Zume.MOD_ID,
	name = Tags.MOD_NAME,
	version = Tags.MOD_VERSION, 
	acceptedMinecraftVersions = Tags.VINTAGE_RANGE,
	dependencies = "required-after:mixinbooter@*")
public class VintageZume extends LegacyForgeZume {
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) 
		throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Zume.LOGGER.info("Loading Vintage Zume...");
		
		//noinspection JavaReflectionMemberAccess
		final Method registerKeyBinding = ClientRegistry.class.getMethod("registerKeyBinding", KeyBinding.class);
		
		for (final ZumeKeyBind keyBind : ZumeKeyBind.values()) {
			registerKeyBinding.invoke(null, keyBind.value);
		}
		
		super.preInit();
	}
	
}
