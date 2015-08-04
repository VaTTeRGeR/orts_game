package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import de.vatterger.entitysystem.components.CircleCollision;
import de.vatterger.entitysystem.components.CircleCollisionOccured;
import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.tools.SpatialVector3Map;
import de.vatterger.entitysystem.tools.Bucket;

public class CircleContainmentProcessor extends EntityProcessingSystem {

	private final static int CELL_SIZE = 16; 

	private ComponentMapper<Position>	posMapper;
	private ComponentMapper<CircleCollision>	cirMapper;
	private Bag<Circle> circles = new Bag<Circle>();
	private SpatialVector3Map<Circle> map = new SpatialVector3Map<Circle>(CELL_SIZE);
	private Rectangle rect = new Rectangle();

	@SuppressWarnings("unchecked")
	public CircleContainmentProcessor() {
		super(Aspect.getAspectForAll(Position.class, CircleCollision.class));
	}

	@Override
	protected void initialize() {
		posMapper = ComponentMapper.getFor(Position.class, world);
		cirMapper = ComponentMapper.getFor(CircleCollision.class, world);
	}

	@Override
	protected void inserted(Entity e) {
		circles.add(cirMapper.get(e).circle);
	}
	
	@Override
	protected void removed(Entity e) {
		circles.remove(cirMapper.get(e).circle);
	}
	
	@Override
	protected void begin() {
		Circle c;
		for (int i = 0; i < circles.size(); i++) {
			c = circles.get(i);
			map.insert(c, c);
		}
	}
	
	@Override
	protected void end() {
		map.clear();
	}
	
	protected void process(Entity e) {
		Position pc = posMapper.get(e);
		
		Circle circle = cirMapper.get(e).circle;
		circle.setPosition(pc.pos.x, pc.pos.y);
		
		Bucket<Circle> b = map.getBucket(getBounds(circle));

		Circle otherCircle;
		for (int i = 0; i < b.size(); i++) {
			otherCircle = b.get(i);
			if(otherCircle.contains(circle) && !circle.equals(otherCircle)){
				e.edit().add(new CircleCollisionOccured(otherCircle));
				//System.out.println("Entity "+e.id+" contained in another circle at "+pc.pos);
			}
		}
	}

	public Rectangle getBounds(Circle circle) {
		rect.set(circle.x-circle.radius, circle.y-circle.radius, circle.radius*2, circle.radius*2);
		return rect;
	}
}
