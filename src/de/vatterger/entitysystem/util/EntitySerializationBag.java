package de.vatterger.entitysystem.util;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.World;
import com.artemis.utils.Bag;

public final class EntitySerializationBag extends Bag<Bag<Component>> {
	public EntitySerializationBag() {
		this(256);
	}
	public EntitySerializationBag(int capacity) {
		super(capacity);
	}

	public void saveEntities(Bag<Entity> entBag, World w) {
		for (int i = 0; i < entBag.size(); i++) {
			saveEntity(entBag.get(i), w);
		}
	}

	public void saveEntity(Entity e, World w) {
		EntitySaveNode node = new EntitySaveNode();
		w.getComponentManager().getComponentsFor(e, node);
		super.add(node);
	}

	private void loadEntity(int i, World w) throws ArrayIndexOutOfBoundsException {
		Bag<Component> entBag = get(i);
		Entity e = w.createEntity();
		for (int j = 0; j < entBag.size(); j++) {
			e.edit().add(entBag.get(j));
		}
	}
	
	public void loadEntities(World w) throws ArrayIndexOutOfBoundsException {
		for (int i = 0; i < size(); i++) {
			loadEntity(i, w);
		}
	}
}