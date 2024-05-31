package net.minecraftforge.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Mod {
	
	// Pre/Modern Forge
	String value() default "";
	
	// Vintage Forge
	String modid() default "";
	String name() default "";
	String version() default "";
	String acceptedMinecraftVersions() default "";
	String guiFactory() default "";
	
}
