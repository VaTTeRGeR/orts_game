package de.vatterger.entitysystem.processors;

import static de.vatterger.entitysystem.util.GameConstants.EXPECTED_ENTITYCOUNT;
import static de.vatterger.entitysystem.util.GameConstants.XY_BOUNDS;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Rectangle;

import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.components.RemoteMaster;
import de.vatterger.entitysystem.util.map.GridPartitionMap;

public class RemoteMasterRemovedProcessor extends EntityProcessingSystem {

	private ComponentMapper<Position>	pm;

	private GridPartitionMap<Integer> removedMap = new GridPartitionMap<Integer>(XY_BOUNDS, EXPECTED_ENTITYCOUNT/100);

	@SuppressWarnings("unchecked")
	public RemoteMasterRemovedProcessor() {
		super(Aspect.getAspectForAll(Position.class, RemoteMaster.class));
	}

	@Override
	protected void initialize() {
		pm = world.getMapper(Position.class);
	}
	
	@Override
	protected void removed(Entity e) {
		removedMap.insert(pm.get(e).pos, e.id);
	}
	
	@Override
	protected void process(Entity e) {
	}
	
	public Bag<Integer> getRemovedEntities(Rectangle r) {
		return removedMap.getBucketsMerged(r);
	}

	public void clearRemovedMap() {
		removedMap.clear();
	}
}
