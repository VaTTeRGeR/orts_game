package de.vatterger.entitysytem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import de.vatterger.entitysystem.components.CircleCollision;
import de.vatterger.entitysystem.components.Position;

public class ContainmentProcessor extends EntityProcessingSystem {

	ComponentMapper<Position>	pm;
	ComponentMapper<CircleCollision>	cm;
	Rectangle[] bounds = new Rectangle[4];

	@SuppressWarnings("unchecked")
	public ContainmentProcessor(int w, int h) {
		super(Aspect.getAspectForAll(Position.class, CircleCollision.class));
		bounds[0] = new Rectangle(-1000, -1000, w+2000, 1000);//lower
		bounds[1] = new Rectangle(-1000, -1000, 1000, h+2000);//left
		bounds[2] = new Rectangle(-1000, h, w+2000, 1000);//upper
		bounds[3] = new Rectangle(w, -1000, 1000, h+2000);//right
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
		for (int i = 0; i < bounds.length; i++) {
			if(Intersector.overlaps(cc.circle, bounds[i])) {
				e.deleteFromWorld();
				//System.out.println("Containment: Deleted entity at "+pc.pos+" with radius "+cc.circle.radius);
			}
		}
	}
}
