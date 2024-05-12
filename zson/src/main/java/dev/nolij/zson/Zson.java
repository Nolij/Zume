package dev.nolij.zson;

import java.util.AbstractMap;
import java.util.Map;

public final class Zson {
	public String indent;
	
	public Zson(String indent) {
		this.indent = indent;
	}
	
	public ZsonMap parse(String json) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public String stringify(ZsonMap zson) {
		StringBuilder sb = new StringBuilder("{\n");
		
		for (var entry : zson.entrySet()) {
			Map.Entry<String, String> keyPair = entry.getKey();
			String key = keyPair.getKey(), comment = keyPair.getValue();
			Object value = entry.getValue();
			
			if (comment != null && !comment.isEmpty()) {
				for (String line : comment.split("\n")) {
					sb.append(indent).append("// ").append(line).append("\n");
				}
			}
			
			sb.append(indent).append('"').append(key).append("\": ").append(value(value)).append(",\n");
		}
		
		return sb.append("}").toString();
	}
	
	private String value(Object value) {
		return switch (value) {
			case ZsonMap zson -> {
				try {
					yield stringify(zson).replace("\n", "\n" + indent);
				} catch (StackOverflowError e) {
					throw new StackOverflowError("Map is circular");
				}
			}
			case String s -> "\"" + s.replace("\"", "\\\"") + "\"";
			case Number n -> n.toString();
			case Boolean b -> b.toString();
			case null -> "null";
			default -> throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
		};
	}
	
	public static Map.Entry<Map.Entry<String, String>, Object> entry(String key, String comment, Object value) {
		return new AbstractMap.SimpleEntry<>(new AbstractMap.SimpleEntry<>(key, comment), value);
	}
	
	public static Map.Entry<Map.Entry<String, String>, Object> entry(String key, Object value) {
		return entry(key, "", value);
	}
}
