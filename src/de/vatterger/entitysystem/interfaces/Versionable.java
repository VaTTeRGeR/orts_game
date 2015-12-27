package de.vatterger.entitysystem.interfaces;

public interface Versionable {
	public abstract void	newVersion();
	public abstract int		getVersion();
	public abstract boolean compareVersion(int v2);
}
