package dev.nolij.zson;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ZsonMap extends LinkedHashMap<Map.Entry<String, String>, Object> {
	public ZsonMap() {
		super();
	}
	
	@SafeVarargs
	public ZsonMap(Map.Entry<Map.Entry<String, String>, Object>... entries) {
		super(entries.length);
		for (var entry : entries) {
			this.put(entry.getKey(), entry.getValue());
		}
	}

	public Object put(String key, String comment, Object value) {
		return this.put(new AbstractMap.SimpleEntry<>(key, comment), value);
	}

	public Object put(String key, Object value) {
		return this.put(new AbstractMap.SimpleEntry<>(key, ""), value);
	}
}
