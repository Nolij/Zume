package dev.nolij.zume.mixin;

import net.minecraft.client.class_1803;
import net.minecraft.client.gui.screen.options.ControlsListWidget;
import net.minecraft.client.render.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ControlsListWidget.class)
public class ControlsListWidgetMixin {
	
	private final class_1803 dummyClass1803 = new class_1803() {
		@Override
		public void method_6700(int i, int j, int k, int l, int m, Tessellator tessellator, int n, int o, boolean bl) { }
		@Override
		public boolean method_6699(int i, int j, int k, int l, int m, int n) { return false; }
		@Override
		public void method_6701(int i, int j, int k, int l, int m, int n) { }
	};
	
	@Inject(method = "method_6697", at = @At("RETURN"), cancellable = true)
	public void zume$method_6697$HEAD(int par1, CallbackInfoReturnable<class_1803> cir) {
		if (cir.getReturnValue() == null) {
			cir.setReturnValue(dummyClass1803);
		}
	}
	
}
