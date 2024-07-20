package dev.nolij.zume.neoforge;

import dev.nolij.zumegradle.proguard.ProGuardKeep;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;

public class NeoZumeConfigScreenFactory implements IConfigScreenFactory {
	
	@Override
	public @NotNull Screen createScreen(@NotNull Minecraft minecraft, @NotNull Screen parent) {
		return new NeoZumeConfigScreen(parent);
	}
	
	@ProGuardKeep
	public @NotNull Screen createScreen(@NotNull ModContainer modContainer, @NotNull Screen parent) {
		return new NeoZumeConfigScreen(parent);
	}
	
}
