package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Circle;

import de.vatterger.entitysystem.components.CircleCollision;
import de.vatterger.entitysystem.components.CircleContainmentOccured;

public class SlimeAbsorbProcessor extends EntityProcessingSystem {

	ComponentMapper<CircleCollision>		cirMapper;
	ComponentMapper<CircleContainmentOccured>	cirOccMapper;

	@SuppressWarnings("unchecked")
	public SlimeAbsorbProcessor() {
		super(Aspect.getAspectForAll(CircleContainmentOccured.class));
	}

	@Override
	protected void initialize() {
		cirMapper = ComponentMapper.getFor(CircleCollision.class, world);
		cirOccMapper = ComponentMapper.getFor(CircleContainmentOccured.class, world);
	}
	
	protected void process(Entity e) {
		final Circle circle = cirMapper.get(e).circle;
		final Circle other = cirOccMapper.get(e).other;

		other.setRadius(getRadiusOfCircle(circle.area()+other.area()));
		//System.out.println("Entity "+e.id+" has been deleted, other circle is now of radius "+other.radius+" at ["+other.x+","+other.y+"]");
		e.deleteFromWorld();
	}
	
	private float getRadiusOfCircle(double areaOfCircle) {
		return (float)Math.sqrt(areaOfCircle/Math.PI);
	}
}
