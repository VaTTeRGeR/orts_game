package de.vatterger.entitysystem.networkmessages;

import com.artemis.Component;
import com.artemis.utils.Bag;

public class D_RemoteMasterUpdate {
	public int id;
	public Bag<Component> components;
	
	public D_RemoteMasterUpdate() {
		
	}

	public D_RemoteMasterUpdate(int id, Bag<Component> components) {
		this.components = components;
		this.id = id;
	}
}