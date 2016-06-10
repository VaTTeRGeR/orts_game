package de.vatterger.techdemo.network.packets.server;

import de.vatterger.techdemo.interfaces.Sizeable;

public class RemoteMasterUpdate implements Sizeable {
	public int id;
	public byte flags;
	public Object[] components;
	
	public RemoteMasterUpdate() {
	}

	public RemoteMasterUpdate(int id, boolean fullUpdate, Object[] components) {
		this.id = id;
		if(fullUpdate)
			flags = 0;
		else
			flags = 2;
		if(components.length == 0)
			flags += 4;
		this.components = components;
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
				.append(id)
				.append(", full-update: ")
				.append(flags == 0)
				.append("Components: ")
				.append(components.length)
		.toString();
	}

	@Override
	public int getSizeInBytes() {
		int size = 4+1+1; //int + byte + overhead
		if(components.length > 0){
			size++;
			for (int i = 0; i < components.length; i++) {
				size += ((Sizeable)components[i]).getSizeInBytes();
			}
		}
		return size;
	}

	public boolean isFullUpdate() {
		return flags == 0 || flags == 4;
	}
}