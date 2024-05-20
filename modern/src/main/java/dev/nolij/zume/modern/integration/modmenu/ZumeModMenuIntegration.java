package dev.nolij.zume.modern.integration.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import dev.nolij.zume.api.platform.v1.ZumeAPI;
import dev.nolij.zume.api.util.v1.MethodHandleHelper;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.function.Function;

import static dev.nolij.zume.impl.ZumeConstants.MOD_ID;

public class ZumeModMenuIntegration implements ModMenuApi {
	
	private static final MethodHandle LITERALTEXT_INIT = MethodHandleHelper.PUBLIC.getConstructorOrNull(
		MethodHandleHelper.PUBLIC.getClassOrNull("net.minecraft.class_2585"),
		MethodType.methodType(Component.class, String.class),
		String.class);
	
	@Override
	public String getModId() {
		return MOD_ID;
	}
	
	@Override
	public Function<Screen, ? extends Screen> getConfigScreenFactory() {
		return (parent) -> {
            try {
	            //noinspection DataFlowIssue
	            return new ModernZumeConfigScreen((Component) LITERALTEXT_INIT.invokeExact(""), parent);
            } catch (Throwable e) {
				ZumeAPI.getLogger().error("Error opening config screen: ", e);
				return null;
            }
        };
	}
	
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return (parent) -> new ModernZumeConfigScreen(Component.literal(""), parent);
	}
	
}
