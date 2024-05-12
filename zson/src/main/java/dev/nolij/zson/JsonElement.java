package dev.nolij.zson;

public class JsonElement {
	public final String key;
	
	// 0/1 if boolean, value if number
	private double value;
	
	// true = floating point, false = non-floating point, null = boolean
	private final Boolean type;
	
	private JsonElement(String key, double value, Boolean type) {
		this.key = key;
		this.value = value;
		this.type = type;
	}
	
	public static JsonElement doubleElement(String key, double value) {
		return new JsonElement(key, value, true);
	}
	
	public static JsonElement intElement(String key, int value) {
		return new JsonElement(key, value, false);
	}
	
	public static JsonElement booleanElement(String key, boolean value) {
		return new JsonElement(key, value ? 1 : 0, null);
	}
	
	public String toString() {
		return key + ": " + (type == null ?
		                     value == 1 ? "true" : "false" 
		                                  : type ? value : (int) value);
	}
	
	public int getAsInt() {
		if(!isInt()) {
			throw new IllegalStateException("Element is not an integer");
		}
		return (int) value;
	}
	
	public double getAsDouble() {
		if(!isDouble()) {
			throw new IllegalStateException("Element is not a double");
		}
		return value;
	}
	
	public boolean getAsBoolean() {
		if(!isBoolean()) {
			throw new IllegalStateException("Element is not a boolean");
		}
		return value == 1;
	}
	
	public boolean isInt() {
		return Boolean.FALSE.equals(type);
	}
	
	public boolean isDouble() {
		return Boolean.TRUE.equals(type);
	}
	
	public boolean isBoolean() {
		return type == null;
	}
}
