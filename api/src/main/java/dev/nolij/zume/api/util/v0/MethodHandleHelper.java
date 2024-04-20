package dev.nolij.zume.api.util.v0;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class MethodHandleHelper {
	
	@SafeVarargs
	public static <T> T firstNonNull(T... options) {
		for (final T option : options)
			if (option != null)
				return option;
		
		return null;
	}
	
	private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
	
	public static Class<?> getClassOrNull(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException ignored) {
			return null;
		}
	}
	
	public static Class<?> getClassOrNull(String... classNames) {
		for (final String className : classNames) {
			try {
				return Class.forName(className);
			} catch (ClassNotFoundException ignored) { }
		}
		
		return null;
	}
	
	public static MethodHandle getMethodOrNull(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		if (clazz == null)
			return null;
		
		try {
			return lookup.unreflect(clazz.getMethod(methodName, parameterTypes));
		} catch (NoSuchMethodException | IllegalAccessException ignored) {
			return null;
		}
	}
	
	public static MethodHandle getMethodOrNull(Class<?> clazz, String methodName, 
	                                           MethodType methodType, Class<?>... parameterTypes) {
		if (clazz == null)
			return null;
		
		try {
			return lookup.unreflect(clazz.getMethod(methodName, parameterTypes))
				.asType(methodType);
		} catch (NoSuchMethodException | IllegalAccessException ignored) {
			return null;
		}
	}
	
	public static MethodHandle getConstructorOrNull(Class<?> clazz, MethodType methodType, Class<?>... parameterTypes) {
		if (clazz == null)
			return null;
		
		try {
			return lookup.unreflectConstructor(clazz.getConstructor(parameterTypes))
				.asType(methodType);
		} catch (NoSuchMethodException | IllegalAccessException ignored) {
			return null;
		}
	}
	
	public static MethodHandle getGetterOrNull(final Class<?> clazz,
	                                           final String fieldName, final Class<?> fieldType) {
		if (clazz == null || fieldType == null)
			return null;
		
		try {
			return lookup.findGetter(clazz, fieldName, fieldType);
		} catch (NoSuchFieldException | IllegalAccessException ignored) {
			return null;
		}
	}
	
	public static MethodHandle getSetterOrNull(final Class<?> clazz,
	                                           final String fieldName, final Class<?> fieldType) {
		if (clazz == null || fieldType == null)
			return null;
		
		try {
			return lookup.findSetter(clazz, fieldName, fieldType);
		} catch (NoSuchFieldException | IllegalAccessException ignored) {
			return null;
		}
	}
	
}
