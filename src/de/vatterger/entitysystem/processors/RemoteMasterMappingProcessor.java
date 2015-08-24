package de.vatterger.entitysystem.processors;

import static de.vatterger.entitysystem.tools.GameConstants.EXPECTED_ENTITYCOUNT;
import static de.vatterger.entitysystem.tools.GameConstants.XY_BOUNDS;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Rectangle;

import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.components.RemoteMaster;
import de.vatterger.entitysystem.tools.GridPartitionMap;

public class RemoteMasterMappingProcessor extends EntityProcessingSystem {

	private ComponentMapper<Position>	pm;

	private GridPartitionMap<Integer> activeMap = new GridPartitionMap<Integer>(XY_BOUNDS, EXPECTED_ENTITYCOUNT);
	private GridPartitionMap<Integer> removedMap = new GridPartitionMap<Integer>(XY_BOUNDS, EXPECTED_ENTITYCOUNT);

	@SuppressWarnings("unchecked")
	public RemoteMasterMappingProcessor() {
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
	protected void begin() {
		clearActiveMap();
		clearRemovedMap();
	}
	
	@Override
	protected void process(Entity e) {
		Position p = pm.get(e);
		activeMap.insert(p.pos, e.id);
	}
	
	public Bag<Integer> getActiveEntities(Rectangle r) {
		return activeMap.getBucketsMerged(r);
	}
	
	public Bag<Integer> getRemovedEntities(Rectangle r) {
		return removedMap.getBucketsMerged(r);
	}

	public void clearActiveMap() {
		activeMap.clear();
	}
	
	public void clearRemovedMap() {
		removedMap.clear();
	}
}
