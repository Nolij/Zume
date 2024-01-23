package dev.nolij.zume.common;

public enum ZumeVariant {
	
	MODERN("modern"),
	PRIMITIVE("primitive"),
	LEGACY("legacy"),
	ARCHAIC_FORGE("archaic"),
	VINTAGE_FORGE("vintage"),
	NEOFORGE("neoforge"),
	LEXFORGE("lexforge"),
	LEXFORGE18("lexforge18"),
	LEXFORGE16("lexforge16"),
	
	;
	
	public final String name;
	
	ZumeVariant(String name) {
		this.name = name;
	}
	
}
