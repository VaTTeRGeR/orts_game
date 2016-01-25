package de.vatterger.entitysystem.processors.server;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
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

@Wire
public class CircleCollisionProcessor extends EntityProcessingSystem {

	private ComponentMapper<ServerPosition>	spm;
	private ComponentMapper<Velocity>	vm;
	private ComponentMapper<CircleCollision>	scm;

	private Bag<Integer> flyweightEntityBag = new Bag<Integer>(64);
	private Circle flyWeightSelfCircle = new Circle();
	private Circle flyWeightOtherCircle = new Circle();
	
	private final static GridMapBitFlag FLAG_COLLISION = new GridMapBitFlag(GridMapBitFlag.COLLISION);

	@SuppressWarnings("unchecked")
	public CircleCollisionProcessor() {
		super(Aspect.getAspectForAll(CircleCollision.class, ActiveCollision.class, GridMapFlag.class).exclude(Inactive.class));
	}
	
	protected void process(Entity e) {
		flyWeightSelfCircle.set(spm.get(e).pos.x, spm.get(e).pos.y, scm.get(e).radius);
		
		GridMapHandler.getEntities(FLAG_COLLISION, flyWeightSelfCircle, flyweightEntityBag);

		for (int i = flyweightEntityBag.size() - 1; i >= 0; i--) {
			Entity otherEntity = world.getEntity(flyweightEntityBag.get(i));
			Vector3 posOther = spm.get(otherEntity).pos;
			flyWeightOtherCircle.set(posOther.x, posOther.y, scm.get(otherEntity).radius);
			if (flyWeightSelfCircle.overlaps(flyWeightOtherCircle) && otherEntity.id != e.id) {
				Velocity vc = vm.get(e);
				float speed = vc.vel.len();
				Vector3 difNor = spm.get(e).pos.cpy().sub(spm.get(otherEntity).pos).nor();
				vc.vel.set(difNor).scl(speed);
			}
		}
		flyweightEntityBag.clear();
	}
}
