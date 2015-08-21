package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;

import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.components.SlimeCollision;
import de.vatterger.entitysystem.tools.Bucket;
import de.vatterger.entitysystem.tools.GameUtil;
import de.vatterger.entitysystem.tools.GridPartitionMap;
import static de.vatterger.entitysystem.tools.GameConstants.*;

public class SlimeCollisionProcessor extends EntityProcessingSystem {

	private ComponentMapper<SlimeCollision>	ccm;
	private ComponentMapper<Position>	pm;

	private Bag<SlimeCollision> ccBag = new Bag<SlimeCollision>();
	private GridPartitionMap<SlimeCollision> collisionMap = new GridPartitionMap<SlimeCollision>(XY_BOUNDS, EXPECTED_ENTITYCOUNT);
	
	private Bag<Bucket<SlimeCollision>> bucketBagFlyWeight = new Bag<Bucket<SlimeCollision>>(4);
	private Rectangle rectFlyWeight = new Rectangle();

	@SuppressWarnings("unchecked")
	public SlimeCollisionProcessor() {
		super(Aspect.getAspectForAll(SlimeCollision.class, Position.class));
	}

	@Override
	protected void initialize() {
		ccm = ComponentMapper.getFor(SlimeCollision.class, world);
		pm = ComponentMapper.getFor(Position.class, world);
	}

	@Override
	protected void inserted(Entity e) {
		ccBag.add(ccm.get(e));
	}

	@Override
	protected void removed(Entity e) {
		ccBag.remove(ccm.get(e));
	}
	
	@Override
	protected void begin() {
		SlimeCollision cc;
		for (int i = 0; i < ccBag.size(); i++) {
			cc = ccBag.get(i);
			cc.circle.setPosition(pm.get(cc.owner).pos);
			collisionMap.insert(GameUtil.circleToRectangle(cc.circle,rectFlyWeight), cc);
		}
	}
	
	protected void process(Entity e) {
		Circle selfCircle = ccm.get(e).circle;
		
		bucketBagFlyWeight = collisionMap.getBuckets(GameUtil.circleToRectangle(selfCircle,rectFlyWeight));
		Bucket<SlimeCollision> currentBucket;
		
		Circle otherCircle;
		Entity otherEntity;
		for (int i = 0; i < bucketBagFlyWeight.size(); i++) {
			currentBucket = bucketBagFlyWeight.get(i);
			for (int j = 0; j < currentBucket.size(); j++) {
				otherCircle = currentBucket.get(j).circle;
				otherEntity = currentBucket.get(j).owner;
				if(selfCircle.contains(otherCircle) && !(otherEntity.id == e.id)){
					selfCircle.setRadius(getRadiusOfCircle(selfCircle.area()+otherCircle.area()));
					//System.out.println("Entity "+e.id+" absorbs entity "+currentBucket.get(j).owner.id+" at "+pc.pos+" and is now of area "+selfCircle.area());
					otherCircle.radius = 0;
					otherEntity.deleteFromWorld();
				}
			}
		}
		bucketBagFlyWeight.clear();
	}

	private float getRadiusOfCircle(double areaOfCircle) {
		return (float)Math.sqrt(areaOfCircle/Math.PI);
	}

	@Override
	protected void end() {
		collisionMap.clear();
	}
}
