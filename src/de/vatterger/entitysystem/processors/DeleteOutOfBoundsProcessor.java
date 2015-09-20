package de.vatterger.entitysystem.processors;

import static de.vatterger.entitysystem.util.GameConstants.*;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;

import de.vatterger.entitysystem.EntityFactory;
import de.vatterger.entitysystem.components.CircleCollision;
import de.vatterger.entitysystem.components.Inactive;
import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.util.GameUtil;

public class DeleteOutOfBoundsProcessor extends EntityProcessingSystem {

	ComponentMapper<Position>	pm;
	ComponentMapper<CircleCollision>	scm;
	Rectangle bounds;
	Rectangle flyweightRectangle = new Rectangle();
	Circle flyweightCircle = new Circle();

	public DeleteOutOfBoundsProcessor() {
		this(0,0,XY_BOUNDS,XY_BOUNDS);
	}
	
	@SuppressWarnings("unchecked")
	public DeleteOutOfBoundsProcessor(int x, int y,int w, int h) {
		super(Aspect.getAspectForAll(Position.class, CircleCollision.class).exclude(Inactive.class));
		bounds = new Rectangle(x,y,w,h);
	}

	@Override
	protected void initialize() {
		pm = world.getMapper(Position.class);
		scm = world.getMapper(CircleCollision.class);
	}

	protected void process(Entity e) {
		flyweightCircle.set(pm.get(e).pos.x,pm.get(e).pos.z, scm.get(e).radius);
		if(!bounds.contains(GameUtil.circleToRectangle(flyweightCircle, flyweightRectangle))) {
			//pc.pos.set(MathUtils.random(0, XY_BOUNDS), MathUtils.random(0, XY_BOUNDS));
			//System.out.println("Containment: Deleted entity at "+pc.pos+" with radius "+cc.circle.radius);
			EntityFactory.deactivateEntity(e);
		}
	}
}
