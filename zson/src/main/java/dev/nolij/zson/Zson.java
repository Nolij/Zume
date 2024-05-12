package dev.nolij.zson;

import java.util.AbstractMap;
import java.util.Map;

public final class Zson {
	public String indent;
	
	public Zson(String indent) {
		this.indent = indent;
	}
	
	public Map<Map.Entry<String, String>, Object> parse(String json) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public String stringify(Map<Map.Entry<String, String>, Object> zson) {
		StringBuilder sb = new StringBuilder("{\n");
		
		for (Map.Entry<Map.Entry<String, String>, Object> entry : zson.entrySet()) {
			Map.Entry<String, String> keyPair = entry.getKey();
			String key = keyPair.getKey(), comment = keyPair.getValue();
			Object value = entry.getValue();
			
			if (!comment.isEmpty()) {
				String[] lines = comment.split("\n");
				for (String line : lines) {
					sb.append(indent).append("// ").append(line).append("\n");
				}
			}
			
			sb.append(indent).append(key).append(": ").append(value(value)).append(",\n");
		}
		
		return sb.append("}").toString();
	}
	
	private String value(Object value) {
		if(value instanceof Map<?, ?>) {
			try {
				//noinspection unchecked
				return stringify((Map<Map.Entry<String, String>, Object>) value)
					.replace("\n", "\n" + indent);
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("Map must have keys of type Map.Entry<String, String>", e);
			} catch (StackOverflowError e) {
				throw new StackOverflowError("Map is circular");
			}
		}
		if (value instanceof String) {
			return "\"" + ((String) value).replace("\"", "\\\"") + "\"";
		} else {
			return String.valueOf(value);
		}
	}
	
	public static Map.Entry<String, String> key(String key, String comment) {
		return new AbstractMap.SimpleEntry<>(key, comment);
	}
}
