package de.vatterger.entitysystem.tools;

public enum ProfileUnit {
	NANOSECONDS(1,"ns"),
	MILLISECONDS(1000000,"ms"),
	SECONDS(1000000000,"s");
	
	long scale;
	String identifier;
	
	private ProfileUnit(long scale, String identifier){
		this.scale = scale;
		this.identifier = identifier;
	}
}
