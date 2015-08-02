package de.vatterger.entitysystem.networkmessages;

public class A_SetName {

	public boolean granted;
	
	public A_SetName() {
		granted = true;
	}

	public A_SetName(boolean granted) {
		this.granted = granted;
	}
}
