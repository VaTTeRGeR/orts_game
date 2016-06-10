package de.vatterger.techdemo.interfaces;

public interface Interpolatable<T> {
	public void		updateInterpolation(float delta, T target, boolean newUpdate);
	public T		getInterpolatedValue();
}
