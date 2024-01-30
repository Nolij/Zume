package dev.nolij.zume.modern.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import dev.nolij.zume.common.Zume;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.function.Function;

public class ZumeModMenuIntegration implements ModMenuApi {
	
	private static final class ModernZumeConfigScreen extends Screen {
		
		private final Screen parent;
		
		private ModernZumeConfigScreen(Text arg, Screen parent) {
			super(arg);
			this.parent = parent;
			Zume.openConfigFile();
		}
		
		@Override
		public void init() {
			MinecraftClient.getInstance().setScreen(parent);
		}
		
	}
	
	@Override
	public String getModId() {
		return Zume.MOD_ID;
	}
	
	private static final MethodHandle LITERALTEXT_INIT;
	
	static {
		final MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodHandle methodHandle = null;
        try {
			final Constructor<?> literalTextInit = LiteralTextContent.class.getConstructor(String.class);
			methodHandle = lookup.unreflectConstructor(literalTextInit);
        } catch (NoSuchMethodException | IllegalAccessException ignored) {}
        
        LITERALTEXT_INIT = methodHandle;
    }
	
	@Override
	public Function<Screen, ? extends Screen> getConfigScreenFactory() {
		return (parent) -> {
            try {
                return new ModernZumeConfigScreen((Text) LITERALTEXT_INIT.invoke(""), parent);
            } catch (Throwable e) {
				Zume.LOGGER.error("Error opening config screen: ", e);
				return null;
            }
        };
	}
	
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return (parent) -> new ModernZumeConfigScreen(Text.literal(""), parent);
	}
	
}
