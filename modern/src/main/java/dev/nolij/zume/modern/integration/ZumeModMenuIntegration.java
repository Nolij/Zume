package dev.nolij.zume.modern.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import dev.nolij.zume.common.Zume;
import dev.nolij.zume.common.util.MethodHandleHelper;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.function.Function;

public class ZumeModMenuIntegration implements ModMenuApi {
	
	private static final MethodHandle LITERALTEXT_INIT = MethodHandleHelper.getConstructorOrNull(
		MethodHandleHelper.getClassOrNull("net.minecraft.class_2585"),
		MethodType.methodType(Text.class, String.class),
		String.class);
	
	private static final class ModernZumeConfigScreen extends Screen {
		private final Screen parent;
		
		private ModernZumeConfigScreen(Text arg, Screen parent) {
			super(arg);
			this.parent = parent;
		}
		
		@Override
		public void init() {
			Zume.openConfigFile();
			
			MinecraftClient.getInstance().setScreen(parent);
		}
		
		public void render(int mouseX, int mouseY, float delta) {
			init();
		}
	}
	
	@Override
	public String getModId() {
		return Zume.MOD_ID;
	}
	
	@Override
	public Function<Screen, ? extends Screen> getConfigScreenFactory() {
		return (parent) -> {
            try {
                return new ModernZumeConfigScreen((Text) LITERALTEXT_INIT.invokeExact(""), parent);
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
