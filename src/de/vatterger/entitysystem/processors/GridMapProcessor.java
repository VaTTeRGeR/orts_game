package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Circle;

import de.vatterger.entitysystem.components.Flag;
import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.components.CircleCollision;
import de.vatterger.entitysystem.gridmapservice.GridFlag;
import de.vatterger.entitysystem.gridmapservice.GridMapService;

public class GridMapProcessor extends EntityProcessingSystem {

	private ComponentMapper<Flag> fm;
	private ComponentMapper<CircleCollision> scm;
	private ComponentMapper<Position> pm;
	private Circle flyWeightCircle = new Circle();

	@SuppressWarnings("unchecked")
	public GridMapProcessor() {
		super(Aspect.getAspectForAll(Position.class, Flag.class));
	}

	@Override
	protected void initialize() {
		fm = world.getMapper(Flag.class);
		scm = world.getMapper(CircleCollision.class);
		pm = world.getMapper(Position.class);
	}
	
	@Override
	protected void begin() {
		GridMapService.clear();
	}

	@Override
	protected void process(Entity e) {
		GridFlag flag = fm.get(e).flag;
		if(flag.hasAllFlagsOf(GridFlag.COLLISION)) {
			flyWeightCircle.set(pm.get(e).pos,scm.get(e).radius);
			GridMapService.insert(flyWeightCircle, e.id, flag);
		} else {
			GridMapService.insert(pm.get(e).pos, e.id, flag);
		}
	}
	
	@Override
	protected void dispose() {
		GridMapService.clear();
	}
}
