package de.vatterger.entitysystem.interfaces;

public interface Modifiable {
	public abstract void setIsModified();
	public abstract void setModificationApplied();
	public abstract boolean getIsModified();
}
