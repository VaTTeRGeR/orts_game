package de.vatterger.entitysystem.interfaces;

public interface SavableWorld {
	public abstract void create() throws Exception;
	public abstract void update(float delta);
	public abstract void dispose();
	public abstract void load() throws Exception;
	public abstract void save() throws Exception;
}
