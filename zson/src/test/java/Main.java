import dev.nolij.zson.Zson;
import dev.nolij.zson.ZsonMap;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
	public static void main(String[] args) throws Throwable {
		Zson zson = new Zson("  ");
		ZsonMap zsonMap = new ZsonMap();
		zsonMap.put("name", "The name of the person\nlook, a second line!", "John Doe");
		zsonMap.put("age", "The age of the person", 30);
		zsonMap.put("address", "The address of the person", new ZsonMap(
			Zson.entry("street", "The street of the address", "123 Main St"),
			Zson.entry("city", "The city of the address", "Springfield"),
			Zson.entry("state", "The state of the address", "IL"),
			Zson.entry("zip", "The zip code of the address", 62701)
		));
		zsonMap.put("phoneNumbers", "The phone numbers of the person", new ZsonMap(
			Zson.entry("home", "217-555-1234"),
			Zson.entry("cell", "217-555-5678")
		));
		Files.write(Paths.get("person.json5"), zson.stringify(zsonMap).getBytes(StandardCharsets.UTF_8));
		
		ZsonMap map2 = new ZsonMap();
		ZsonMap map3 = new ZsonMap();
		map3.put("test", "a", map2);
		map2.put("test", "b", map3);
		
		System.out.println(zson.stringify(map2));
	}
}
