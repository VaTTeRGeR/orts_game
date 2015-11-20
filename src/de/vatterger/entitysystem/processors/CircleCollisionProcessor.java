package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.components.server.ServerPosition;
import de.vatterger.entitysystem.components.shared.ActiveCollision;
import de.vatterger.entitysystem.components.shared.CircleCollision;
import de.vatterger.entitysystem.components.shared.GridMapFlag;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.components.shared.Velocity;
import de.vatterger.entitysystem.handler.gridmap.GridMapBitFlag;
import de.vatterger.entitysystem.handler.gridmap.GridMapHandler;

public class CircleCollisionProcessor extends EntityProcessingSystem {

	private ComponentMapper<ServerPosition>	pm;
	private ComponentMapper<Velocity>	vm;
	private ComponentMapper<CircleCollision>	scm;
	private ComponentMapper<ActiveCollision>	acm;

	private Bag<Integer> entityBagFlyWeight = new Bag<Integer>(64);
	
	private GridMapBitFlag colFlag = new GridMapBitFlag(GridMapBitFlag.COLLISION);

	private Circle flyWeightselfCircle = new Circle();
	private Circle flyWeightOtherCircle = new Circle();
	
	@SuppressWarnings("unchecked")
	public CircleCollisionProcessor() {
		super(Aspect.getAspectForAll(CircleCollision.class, ActiveCollision.class, GridMapFlag.class).exclude(Inactive.class));
	}

	@Override
	protected void initialize() {
		pm = ComponentMapper.getFor(ServerPosition.class, world);
		vm = ComponentMapper.getFor(Velocity.class, world);
		scm = ComponentMapper.getFor(CircleCollision.class, world);
		acm = ComponentMapper.getFor(ActiveCollision.class, world);
	}
	
	protected void process(Entity e) {
		if(acm.getSafe(e) == null) {
			return;
		}


		flyWeightselfCircle.set(pm.get(e).pos.x, pm.get(e).pos.y, scm.get(e).radius);
		
		GridMapHandler.getEntities(colFlag, flyWeightselfCircle, entityBagFlyWeight);
		Entity otherEntity;

		for (int i = entityBagFlyWeight.size()-1; i >= 0; i--) {
			otherEntity = world.getEntity(entityBagFlyWeight.get(i));
			flyWeightOtherCircle.set(pm.get(otherEntity).pos.x,pm.get(otherEntity).pos.y, scm.get(otherEntity).radius);
			if(flyWeightselfCircle.overlaps(flyWeightOtherCircle) && otherEntity.id != e.id) {
				Velocity vc = vm.get(e);
				float speed = vc.vel.len();
				Vector3 difNor = pm.get(e).pos.cpy().sub(pm.get(otherEntity).pos).nor();
				vc.vel.set(difNor).scl(speed);
			}
		}
		entityBagFlyWeight.clear();
	}
}
