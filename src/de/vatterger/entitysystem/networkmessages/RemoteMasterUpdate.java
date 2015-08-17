package de.vatterger.entitysystem.networkmessages;

import com.artemis.Component;
import com.artemis.utils.Bag;

public class RemoteMasterUpdate {
	public int id;
	public Bag<Component> components;
	
	public RemoteMasterUpdate() {
		
	}

	public RemoteMasterUpdate(int id, Bag<Component> components) {
		this.components = components;
		this.id = id;
	}
}