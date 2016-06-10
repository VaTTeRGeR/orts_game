package de.vatterger.techdemo.quadtree;

import com.badlogic.gdx.math.Rectangle;

public class SpatialEntry<T> {
	
	private Rectangle r;
	private T o;
	
	public SpatialEntry(Rectangle r, T o) {
		this.r = new Rectangle(r);
		this.o = o;
	}
	
	public Rectangle getSpatial() {
		return r;
	}

	public void setSpatial(Rectangle r) {
		r.set(r);
	}
	
	public T getEntry() {
		return o;
	}
	
	@Override
	public int hashCode() {
		return ((int)r.x)^31*((int)r.y)^73;
	}
}