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
			String key = keyPair.getKey();
			String comment = keyPair.getValue();
			Object value = entry.getValue();
			
			if (!comment.isEmpty()) {
				writeComment(sb, comment);
			}
			
			sb.append(indent).append(key).append(": ");
			if (value instanceof Map<?, ?>) {
				//noinspection unchecked
				String inner = stringify((Map<Map.Entry<String, String>, Object>) value);
				sb.append(inner.replace("\n", "\n" + indent));
			} else {
				sb.append(value(value));
			}
			
			sb.append(",\n");
		}
		
		return sb.append("}").toString();
	}
	
	private void writeComment(StringBuilder sb, String comment) {
		String[] lines = comment.split("\n");
		for (String line : lines) {
			sb.append(indent).append("// ").append(line).append("\n");
		}
	}
	
	private String value(Object value) {
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
