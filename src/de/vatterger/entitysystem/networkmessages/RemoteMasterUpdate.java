package de.vatterger.entitysystem.networkmessages;

public class RemoteMasterUpdate {
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
}