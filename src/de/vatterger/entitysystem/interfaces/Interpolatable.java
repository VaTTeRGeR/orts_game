package de.vatterger.entitysystem.interfaces;

public interface Interpolatable<T> {
	public void		updateInterpolation(float delta, T target);
	public T 		getInterpolatedValue();
}
