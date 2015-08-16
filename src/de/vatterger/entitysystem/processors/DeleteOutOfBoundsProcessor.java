package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Rectangle;

import de.vatterger.entitysystem.components.CircleCollision;
import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.tools.GameUtil;

import static de.vatterger.entitysystem.tools.GameConstants.*;

public class DeleteOutOfBoundsProcessor extends EntityProcessingSystem {

	ComponentMapper<Position>	pm;
	ComponentMapper<CircleCollision>	cm;
	Rectangle bounds;
	Rectangle flyweight;

	public DeleteOutOfBoundsProcessor() {
		this(0,0,XY_BOUNDS,XY_BOUNDS);
	}
	
	@SuppressWarnings("unchecked")
	public DeleteOutOfBoundsProcessor(int x, int y,int w, int h) {
		super(Aspect.getAspectForAll(Position.class, CircleCollision.class));
		bounds = new Rectangle(x,y,w,h);
		flyweight = new Rectangle();
	}

	@Override
	protected void initialize() {
		pm = world.getMapper(Position.class);
		cm = world.getMapper(CircleCollision.class);
	}

	protected void process(Entity e) {
		Position pc = pm.get(e);
		CircleCollision cc = cm.get(e);

		cc.circle.setPosition(pc.pos.x, pc.pos.y);
		if(!bounds.contains(GameUtil.circleToRectangle(cc.circle, flyweight))) {
			e.deleteFromWorld();
			//System.out.println("Containment: Deleted entity at "+pc.pos+" with radius "+cc.circle.radius);
		}
	}
}
