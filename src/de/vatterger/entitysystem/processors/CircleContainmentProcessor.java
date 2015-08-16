package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;

import de.vatterger.entitysystem.components.CircleCollision;
import de.vatterger.entitysystem.components.CircleContainmentOccured;
import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.tools.Profiler;
import de.vatterger.entitysystem.tools.SpatialPartitionMap;
import de.vatterger.entitysystem.tools.Bucket;

import static de.vatterger.entitysystem.tools.GameConstants.*;

public class CircleContainmentProcessor extends EntityProcessingSystem {

	private ComponentMapper<Position>	posMapper;
	private ComponentMapper<CircleCollision>	cirMapper;
	private Bag<Circle> dynamicCircles = new Bag<Circle>();
	private SpatialPartitionMap<Circle> dynamicMap = new SpatialPartitionMap<Circle>(XY_BOUNDS, EXPECTED_ENTITYCOUNT);
	private Bag<Bucket<Circle>> bucketBag = new Bag<Bucket<Circle>>(4);
	private Rectangle rectFlyWeight = new Rectangle();
	Profiler p;
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
		dynamicCircles.add(cirMapper.get(e).circle);
	}

	@Override
	protected void removed(Entity e) {
		dynamicCircles.remove(cirMapper.get(e).circle);
	}
	
	@Override
	protected void begin() {
		Circle c;
		for (int i = 0; i < dynamicCircles.size(); i++) {
			c = dynamicCircles.get(i);
			dynamicMap.insert(getBounds(c), c);
		}
	}
	
	@Override
	protected void end() {
		dynamicMap.clear();
	}
	
	protected void process(Entity e) {
		Position pc = posMapper.get(e);
		
		Circle circle = cirMapper.get(e).circle;
		circle.setPosition(pc.pos.x, pc.pos.y);
		
		bucketBag = dynamicMap.getBuckets(getBounds(circle));
		Bucket<Circle> b;
		
		Circle otherCircle;
		for (int i = 0; i < bucketBag.size(); i++) {
			b = bucketBag.removeLast();
			for (int j = 0; j < b.size(); j++) {
				otherCircle = b.get(j);
				if(otherCircle.contains(circle) && !circle.equals(otherCircle)){
					e.edit().add(new CircleContainmentOccured(otherCircle));
					//System.out.println("Entity "+e.id+" contained in another circle at "+pc.pos);
				}
			}
		}
	}

	public Rectangle getBounds(Circle circle) {
		rectFlyWeight.set(circle.x-circle.radius, circle.y-circle.radius, circle.radius*2, circle.radius*2);
		return rectFlyWeight;
	}
}
