package com.artemis.managers;

import java.util.HashMap;
import java.util.Map;

import com.artemis.Entity;
import com.artemis.Manager;
import com.artemis.utils.Bag;

public class RemoteIDManager extends Manager {
	private final Map<Integer, Entity> idToEntity;
	private final Bag<Integer> entityToId;

	public RemoteIDManager() {
		this.idToEntity = new HashMap<Integer, Entity>();
		this.entityToId = new Bag<Integer>();
	}

	@Override
	public void deleted(Entity e) {
		Integer id = entityToId.safeGet(e.getId());
		if (id == null)
			return;
		
		idToEntity.remove(id);
		entityToId.set(e.getId(), null);
	}
	
	public void updatedId(Entity e, int newId) {
		Integer oldid = entityToId.safeGet(e.getId());
		if (oldid != null)
			idToEntity.remove(oldid);
		
		setId(e, newId);
	}
	
	public Entity getEntity(int id) {
		return idToEntity.get(id);
	}

	public int getId(Entity e) {
		Integer id = entityToId.safeGet(e.getId());
		if (id == null) {
			id = -1;
			setId(e, id);
		}
		
		return id;
	}
	
	public void setId(Entity e, int newId) {
		Integer oldId = entityToId.safeGet(e.getId());
		if (oldId != null)
			idToEntity.remove(oldId);
		
		idToEntity.put(newId, e);
		entityToId.set(e.getId(), newId);
	}
}
