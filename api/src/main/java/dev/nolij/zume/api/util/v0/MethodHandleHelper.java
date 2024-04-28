package dev.nolij.zume.api.util.v0;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.Objects;

public class MethodHandleHelper {
	
	public static final MethodHandleHelper PUBLIC =
		new MethodHandleHelper(MethodHandleHelper.class.getClassLoader(), MethodHandles.lookup());
	
	private final @NotNull ClassLoader classLoader;
	private final @NotNull MethodHandles.Lookup lookup;
	
	public MethodHandleHelper(@NotNull final ClassLoader classLoader, @NotNull final MethodHandles.Lookup lookup) {
		this.classLoader = classLoader;
		this.lookup = lookup;
	}
	
	@SafeVarargs
	public static <T> @Nullable T firstNonNull(@Nullable T... options) {
		for (final T option : options)
			if (option != null)
				return option;
		
		return null;
	}
	
	public @Nullable Class<?> getClassOrNull(@NotNull final String className) {
		try {
			return Class.forName(className, true, classLoader);
		} catch (ClassNotFoundException ignored) {
			return null;
		}
	}
	
	public @Nullable Class<?> getClassOrNull(@NotNull String... classNames) {
		for (final String className : classNames) {
			try {
				return Class.forName(className, true, classLoader);
			} catch (ClassNotFoundException ignored) { }
		}
		
		return null;
	}
	
	public @Nullable MethodHandle getMethodOrNull(@Nullable final Class<?> clazz, 
	                                              @NotNull final String methodName,
	                                              @Nullable Class<?>... parameterTypes) {
		if (clazz == null || Arrays.stream(parameterTypes).anyMatch(Objects::isNull))
			return null;
		
		try {
			return lookup.unreflect(clazz.getMethod(methodName, parameterTypes));
		} catch (NoSuchMethodException | IllegalAccessException ignored) {
			return null;
		}
	}
	
	public @Nullable MethodHandle getMethodOrNull(@Nullable final Class<?> clazz, 
	                                              @NotNull final String methodName,
	                                              @Nullable final MethodType methodType,
	                                              @Nullable Class<?>... parameterTypes) {
		if (clazz == null || Arrays.stream(parameterTypes).anyMatch(Objects::isNull))
			return null;
		
		try {
			return lookup.unreflect(clazz.getMethod(methodName, parameterTypes))
				.asType(methodType);
		} catch (NoSuchMethodException | IllegalAccessException ignored) {
			return null;
		}
	}
	
	public @Nullable MethodHandle getConstructorOrNull(@Nullable final Class<?> clazz,
	                                                   @NotNull final MethodType methodType,
	                                                   @Nullable Class<?>... parameterTypes) {
		if (clazz == null || Arrays.stream(parameterTypes).anyMatch(Objects::isNull))
			return null;
		
		try {
			return lookup.unreflectConstructor(clazz.getConstructor(parameterTypes))
				.asType(methodType);
		} catch (NoSuchMethodException | IllegalAccessException ignored) {
			return null;
		}
	}
	
	public @Nullable MethodHandle getGetterOrNull(@Nullable final Class<?> clazz, @NotNull final String fieldName,
	                                              @Nullable final Class<?> fieldType) {
		if (clazz == null || fieldType == null)
			return null;
		
		try {
			return lookup.findGetter(clazz, fieldName, fieldType);
		} catch (NoSuchFieldException | IllegalAccessException ignored) {
			return null;
		}
	}
	
	public @Nullable MethodHandle getGetterOrNull(@Nullable final Class<?> clazz, @NotNull final String fieldName,
	                                              @Nullable final Class<?> fieldType, @NotNull final MethodType methodType) {
		if (clazz == null || fieldType == null)
			return null;
		
		try {
			return lookup.findGetter(clazz, fieldName, fieldType)
				.asType(methodType);
		} catch (NoSuchFieldException | IllegalAccessException ignored) {
			return null;
		}
	}
	
	public @Nullable MethodHandle getSetterOrNull(@Nullable final Class<?> clazz, @NotNull final String fieldName,
	                                              @Nullable final Class<?> fieldType) {
		if (clazz == null || fieldType == null)
			return null;
		
		try {
			return lookup.findSetter(clazz, fieldName, fieldType);
		} catch (NoSuchFieldException | IllegalAccessException ignored) {
			return null;
		}
	}
	
	public @Nullable MethodHandle getSetterOrNull(@Nullable final Class<?> clazz, @NotNull final String fieldName,
	                                              @Nullable final Class<?> fieldType, @NotNull final MethodType methodType) {
		if (clazz == null || fieldType == null)
			return null;
		
		try {
			return lookup.findSetter(clazz, fieldName, fieldType)
				.asType(methodType);
		} catch (NoSuchFieldException | IllegalAccessException ignored) {
			return null;
		}
	}
	
}
