package dev.nolij.zume.modern.integration.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import dev.nolij.libnolij.refraction.Refraction;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.function.Function;

import static dev.nolij.zume.impl.ZumeConstants.MOD_ID;

public class ZumeModMenuIntegration implements ModMenuApi {
	
	private static final MethodHandle LITERALTEXT_INIT = Refraction.safe().getConstructorOrNull(
		Refraction.safe().getClassOrNull("net.minecraft.class_2585"),
		MethodType.methodType(Component.class, String.class),
		String.class);
	
	@Override
	public String getModId() {
		return MOD_ID;
	}
	
	@Override
	public Function<Screen, ? extends Screen> getConfigScreenFactory() {
		return (parent) -> {
            //noinspection DataFlowIssue
            return new ModernZumeConfigScreen((Component) LITERALTEXT_INIT.invokeExact(""), parent);
        };
	}
	
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return (parent) -> new ModernZumeConfigScreen(Component.literal(""), parent);
	}
	
}
