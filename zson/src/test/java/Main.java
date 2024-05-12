import dev.nolij.zson.Zson;

import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
	public static void main(String[] args) {
		Zson zson = new Zson("  ");
		Map<Map.Entry<String, String>, Object> zsonMap = new LinkedHashMap<>();
		zsonMap.put(Zson.key("name", "The name of the person\nlook, a second line!"), "John Doe");
		zsonMap.put(Zson.key("age", "The age of the person"), 30);
		zsonMap.put(Zson.key("address", "The address of the person"), map(
			new Map.Entry[] {Zson.key("street", "The street of the address"), Zson.key("city", "The city of the address")},
			new Object[] {"1234 Elm St", "Springfield"}
		));
		System.out.println(zson.stringify(zsonMap));
		
		Map<Map.Entry<String, String>, Object> map2 = new LinkedHashMap<>();
		Map<Map.Entry<String, String>, Object> map3 = new LinkedHashMap<>();
		map3.put(Zson.key("test", "a"), map2);
		map2.put(Zson.key("test", "b"), map3);
		
		System.out.println(zson.stringify(map2));
	}
	
	private static <K, V> Map<K, V> map(K[] keys, V[] values) {
		if(keys.length != values.length) {
			throw new IllegalArgumentException("Keys and values must have the same length");
		}
		Map<K, V> map = new LinkedHashMap<>();
		for (int i = 0; i < keys.length; i++) {
			map.put(keys[i], values[i]);
		}
		return map;
	}
}
