package dev.nolij.zson;

import java.util.LinkedHashMap;
import java.util.Map;

public class ZsonMap extends LinkedHashMap<ZsonKey, Object> {
	public ZsonMap() {
		super();
	}
	
	@SafeVarargs
	public ZsonMap(Map.Entry<ZsonKey, Object>... entries) {
		super(entries.length);
		for (var entry : entries) {
			this.put(entry.getKey(), entry.getValue());
		}
	}

	public ZsonMap put(String key, String comment, Object value) {
		this.put(new ZsonKey(key, comment), value);
		return this;
	}

	public ZsonMap put(String key, Object value) {
		this.put(new ZsonKey(key), value);
		return this;
	}
	
	public Object get(String key) {
		return this.get(new ZsonKey(key));
	}
}
