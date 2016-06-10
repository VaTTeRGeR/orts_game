package de.vatterger.techdemo.interfaces;

public interface Versionable {
	public abstract void	newVersion();
	public abstract int		getVersion();
	public abstract boolean compareVersion(int v);
}
