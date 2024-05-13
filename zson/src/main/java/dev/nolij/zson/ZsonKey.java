package dev.nolij.zson;

public class ZsonKey {
	public final String key;
	public String comment;
	
	public ZsonKey(String key, String comment) {
		this.key = key;
		this.comment = comment;
	}
	
	public ZsonKey(String key) {
		this(key, null);
	}
	
	@Override
	public int hashCode() {
		return key.hashCode();
	}
}
