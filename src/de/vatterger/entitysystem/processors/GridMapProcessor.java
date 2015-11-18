package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;

import de.vatterger.entitysystem.components.server.ServerPosition;
import de.vatterger.entitysystem.components.shared.CircleCollision;
import de.vatterger.entitysystem.components.shared.GridMapFlag;
import de.vatterger.entitysystem.gridmap.GridMapBitFlag;
import de.vatterger.entitysystem.gridmap.GridMapService;

public class GridMapProcessor extends EntityProcessingSystem {

	private ComponentMapper<GridMapFlag> fm;
	private ComponentMapper<CircleCollision> scm;
	private ComponentMapper<ServerPosition> pm;
	private Circle flyWeightCircle = new Circle();
	private Vector2 flyweightVector2 = new Vector2();

	@SuppressWarnings("unchecked")
	public GridMapProcessor() {
		super(Aspect.getAspectForAll(ServerPosition.class, GridMapFlag.class));
	}

	@Override
	protected void initialize() {
		fm = world.getMapper(GridMapFlag.class);
		scm = world.getMapper(CircleCollision.class);
		pm = world.getMapper(ServerPosition.class);
	}
	
	@Override
	protected void begin() {
		GridMapService.clear();
	}

	@Override
	protected void process(Entity e) {
		GridMapBitFlag flag = fm.get(e).flag;
		if(flag.isSuperSetOf(GridMapBitFlag.COLLISION)) {
			flyWeightCircle.set(pm.get(e).pos.x,pm.get(e).pos.y, scm.get(e).radius);
			GridMapService.insert(flyWeightCircle, e.id, flag);
		} else {
			GridMapService.insert(flyweightVector2.set(pm.get(e).pos.x, pm.get(e).pos.y), e.id, flag);
		}
	}
	
	@Override
	protected void dispose() {
		GridMapService.clear();
	}
}
