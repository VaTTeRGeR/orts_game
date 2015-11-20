package de.vatterger.entitysystem.network.packets;

import de.vatterger.entitysystem.interfaces.Sizeable;

public class RemoteMasterUpdate implements Sizeable {
	public int id;
	public boolean fullUpdate;
	public Object[] components;
	
	public RemoteMasterUpdate() {
	}

	public RemoteMasterUpdate(int id, boolean fullUpdate, Object[] components) {
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
				.append(components.length)
		.toString();
	}

	@Override
	public int getSizeInBytes() {
		int size = 4+1+1; //int + boolean + overhead
		if(components.length == 0){
			size += 1;
		} else {
			for (int i = 0; i < components.length; i++) {
				size += ((Sizeable)components[i]).getSizeInBytes();
			}
		}
		return size;
	}
}