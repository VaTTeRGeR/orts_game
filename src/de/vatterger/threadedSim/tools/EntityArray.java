package de.vatterger.threadedSim.tools;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class EntityArray extends Array<Entity> {
	private ObjectMap<Long, Integer> map = new ObjectMap<Long, Integer>();
	
	public EntityArray() {
		super(false, 16);
	}
	
	@Override
	public void add(Entity value) {
		map.put(value.getId(), size);
		super.add(value);
	}
	
	@Override
	public boolean removeValue(Entity value, boolean identity) {
		Integer i = map.remove(value.getId());
		if(i!=null && identity)
			return removeIndex(i) != null;
		else
			return super.removeValue(value, identity);
	}
	
	@Override
	public Entity removeIndex (int index) {
		if (index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		Object[] items = this.items;
		Entity value = (Entity)items[index];
		size--;
		if (ordered) {
			System.arraycopy(items, index + 1, items, index, size - index);
		} else {
			items[index] = items[size];
			map.put(((Entity)items[index]).getId(), index);
		}
		items[size] = null;
		return value;
	}
	@Override
	public void clear() {
		map.clear();
		super.clear();
	}
}
