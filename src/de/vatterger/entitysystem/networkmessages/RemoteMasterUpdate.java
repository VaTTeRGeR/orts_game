package de.vatterger.entitysystem.networkmessages;

import com.artemis.utils.Bag;

import de.vatterger.entitysystem.interfaces.Modifiable;

public class RemoteMasterUpdate {
	public int id;
	public boolean fullUpdate;
	public Bag<Modifiable> components;
	
	public RemoteMasterUpdate() {
	}

	public RemoteMasterUpdate(int id, boolean fullUpdate, Bag<Modifiable> components) {
		this.id = id;
		this.fullUpdate = fullUpdate;
		this.components = components;
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
				.append(id)
				.append(", full-update: ")
				.append(fullUpdate)
				.append("Components: ")
				.append(components.size())
		.toString();
	}
}