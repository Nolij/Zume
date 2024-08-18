package dev.nolij.zume.mixin.modern;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.nolij.zume.impl.Zume;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	
	@Dynamic
	@Inject(method = {"render", "method_3192(Lnet/minecraft/class_9779;Z)V"}, at = @At("HEAD"))
	public void zume$render$HEAD(CallbackInfo ci) {
		Zume.renderHook();
	}
	
	@Group(name = "zume$getFov", min = 1, max = 1)
	@ModifyReturnValue(method = "getFov", at = @At("TAIL"), require = 0)
	public double zume$getFov$TAIL(double original) {
		if (Zume.isFOVHookActive())
			return Zume.fovHook(original);
		
		return original;
	}
	
	// 24w33a (21.2)+ compat
	@Dynamic
	@Group(name = "zume$getFov", min = 1, max = 1)
	@ModifyReturnValue(method = "method_3196(Lnet/minecraft/class_4184;FZ)F", at = @At("TAIL"), require = 0)
	public float zume$getFov$TAIL(float original) {
		if (Zume.isFOVHookActive())
			return (float) Zume.fovHook(original);
		
		return original;
	}
	
}
