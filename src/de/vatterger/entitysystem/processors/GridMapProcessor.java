package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import de.vatterger.entitysystem.components.Flag;
import de.vatterger.entitysystem.components.ServerPosition;
import de.vatterger.entitysystem.components.CircleCollision;
import de.vatterger.entitysystem.gridmapservice.GridFlag;
import de.vatterger.entitysystem.gridmapservice.GridMapService;
import de.vatterger.entitysystem.quadtreeservice.Quadtree;
import de.vatterger.entitysystem.util.GameConstants;
import de.vatterger.entitysystem.util.GameUtil;

public class GridMapProcessor extends EntityProcessingSystem {

	private ComponentMapper<Flag> fm;
	private ComponentMapper<CircleCollision> scm;
	private ComponentMapper<ServerPosition> pm;
	private Circle flyWeightCircle = new Circle();
	private Vector2 flyweightVector2 = new Vector2();

	@SuppressWarnings("unchecked")
	public GridMapProcessor() {
		super(Aspect.getAspectForAll(ServerPosition.class, Flag.class));
	}

	@Override
	protected void initialize() {
		fm = world.getMapper(Flag.class);
		scm = world.getMapper(CircleCollision.class);
		pm = world.getMapper(ServerPosition.class);
	}
	
	@Override
	protected void begin() {
		GridMapService.clear();
	}

	@Override
	protected void process(Entity e) {
		GridFlag flag = fm.get(e).flag;
		if(flag.isSuperSetOf(GridFlag.COLLISION)) {
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
