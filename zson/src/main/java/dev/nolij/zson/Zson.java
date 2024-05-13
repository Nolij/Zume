package dev.nolij.zson;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
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
		StringWriter writer = new StringWriter();
		try {
			write(zson, writer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return writer.toString();
	}
	
	public void write(ZsonMap zson, Writer writer) throws IOException {
		writer.write("{\n");
		
		for (var entry : zson.entrySet()) {
			ZsonKey keyPair = entry.getKey();
			String key = keyPair.key, comment = keyPair.comment;
			Object value = entry.getValue();
			
			if (comment != null) {
				for (String line : comment.split("\n")) {
					writer.write(indent);
					writer.write("// ");
					writer.write(line);
					writer.write("\n");
				}
			}
			
			writer.write(indent);
			writer.write('"');
			writer.write(key);
			writer.write("\": ");
			writer.write(value(value));
			writer.write(",\n");
		}
		
		writer.write("}");
		writer.flush();
	}
	
	private String value(Object value) {
		if(value instanceof ZsonMap zson) {
			try {
				return stringify(zson).replace("\n", "\n" + indent);
			} catch (StackOverflowError e) {
				throw new StackOverflowError("Map is circular");
			}
		} else if(value instanceof String s) {
			return '"' + s
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\t", "\\t")
				+ '"';
		} else if(value instanceof Number || value instanceof Boolean || value == null) {
			return String.valueOf(value);
		} else {
			throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
		}
	}
	
	public static Map.Entry<ZsonKey, Object> entry(String key, String comment, Object value) {
		return new AbstractMap.SimpleEntry<>(new ZsonKey(key, comment), value);
	}
	
	public static Map.Entry<ZsonKey, Object> entry(String key, Object value) {
		return new AbstractMap.SimpleEntry<>(new ZsonKey(key), value);
	}
}
