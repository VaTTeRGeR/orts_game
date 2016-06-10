package de.vatterger.techdemo.processors.server;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.techdemo.components.server.ServerPosition;
import de.vatterger.techdemo.components.shared.ActiveCollision;
import de.vatterger.techdemo.components.shared.CircleCollision;
import de.vatterger.techdemo.components.shared.GridMapFlag;
import de.vatterger.techdemo.components.shared.Inactive;
import de.vatterger.techdemo.components.shared.Velocity;
import de.vatterger.techdemo.handler.gridmap.GridMapBitFlag;
import de.vatterger.techdemo.handler.gridmap.GridMapHandler;

@Wire
public class CircleCollisionProcessor extends EntityProcessingSystem {

	private ComponentMapper<Inactive> iam;
	private ComponentMapper<ServerPosition>	spm;
	private ComponentMapper<Velocity>	vm;
	private ComponentMapper<CircleCollision>	scm;

	private Bag<Integer> flyweightEntityBag = new Bag<Integer>(64);
	private Circle flyWeightSelfCircle = new Circle();
	private Circle flyWeightOtherCircle = new Circle();
	private Vector3 flyWeightVec3 = new Vector3();
	
	private final static GridMapBitFlag GMBF_COLLISION = new GridMapBitFlag(GridMapBitFlag.COLLISION);

	@SuppressWarnings("unchecked")
	public CircleCollisionProcessor() {
		super(Aspect.all(CircleCollision.class, ActiveCollision.class, GridMapFlag.class).exclude(Inactive.class));
	}
	
	protected void process(Entity e) {
		flyWeightSelfCircle.set(spm.get(e).pos.x, spm.get(e).pos.y, scm.get(e).radius);
		
		GridMapHandler.getEntities(GMBF_COLLISION, flyWeightSelfCircle, flyweightEntityBag);

		for (int i = flyweightEntityBag.size() - 1; i >= 0; i--) {
			Entity otherEntity = world.getEntity(flyweightEntityBag.get(i));
			Vector3 posOther = spm.get(otherEntity).pos;
			flyWeightOtherCircle.set(posOther.x, posOther.y, scm.get(otherEntity).radius);
			if (flyWeightSelfCircle.overlaps(flyWeightOtherCircle) && otherEntity.getId() != e.getId() &! iam.has(otherEntity)) {
				Velocity vc = vm.get(e);
				float speed = vc.vel.len();
				//Vector3 difNor = spm.get(e).pos.cpy().sub(spm.get(otherEntity).pos).nor();
				Vector3 difNor = flyWeightVec3.set(spm.get(e).pos).sub(spm.get(otherEntity).pos).nor();
				vc.vel.set(difNor).scl(speed);
				//EntityModifyFactory.deactivateEntityOnGridmap(otherEntity);
			}
		}
		flyweightEntityBag.clear();
	}
}
