package de.vatterger.entitysystem.interfaces;

public interface Modifiable {
	public abstract void	setIsModified();
	public abstract int		getModifiedVersion();
	public abstract boolean getIsModified(int v2);
}
