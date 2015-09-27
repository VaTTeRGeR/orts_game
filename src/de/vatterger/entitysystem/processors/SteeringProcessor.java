package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.components.Inactive;
import de.vatterger.entitysystem.components.ServerPosition;
import de.vatterger.entitysystem.components.ServerRotation;
import de.vatterger.entitysystem.components.Velocity;

public class SteeringProcessor extends EntityProcessingSystem {

	ComponentMapper<Velocity>	vm;
	ComponentMapper<ServerRotation>	rm;

	@SuppressWarnings("unchecked")
	public SteeringProcessor() {
		super(Aspect.getAspectForAll(ServerPosition.class, Velocity.class).exclude(Inactive.class));
	}

	@Override
	protected void initialize() {
		vm = world.getMapper(Velocity.class);
		rm = world.getMapper(ServerRotation.class);
	}

	protected void process(Entity e) {
		Velocity vc = vm.get(e);
		ServerRotation rc = rm.get(e);
		
		/*float rotDelta = 0f;
		if(MathUtils.random(1f)>0.9) {
			rotDelta = MathUtils.random(-30, 30f);
			rc.setIsModified();
		}
		vc.vel.rotate(new Vector3(0f, 0f, 1f), rotDelta);*/
		rc.rot = MathUtils.atan2(vc.vel.y, vc.vel.x)*MathUtils.radiansToDegrees;
	}
}
