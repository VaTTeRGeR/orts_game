package de.vatterger.entitysystem.processors;

import static de.vatterger.entitysystem.util.Constants.*;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;

import de.vatterger.entitysystem.Main;
import de.vatterger.entitysystem.components.ActiveCollision;
import de.vatterger.entitysystem.components.Flag;
import de.vatterger.entitysystem.components.PassiveCollision;
import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.components.CircleCollision;
import de.vatterger.entitysystem.gridmapservice.GridFlag;
import de.vatterger.entitysystem.gridmapservice.GridMapService;
import de.vatterger.entitysystem.util.Functions;
import de.vatterger.entitysystem.util.map.Bucket;
import de.vatterger.entitysystem.util.map.GridPartitionMap;

public class SlimeCollisionProcessor extends EntityProcessingSystem {

	private ComponentMapper<Position>	pm;
	private ComponentMapper<CircleCollision>	scm;
	private ComponentMapper<ActiveCollision>	acm;

	private Bag<Integer> entityBagFlyWeight = new Bag<Integer>(64);
	
	private GridFlag colFlag = new GridFlag(GridFlag.COLLISION);

	private Circle flyWeightselfCircle = new Circle();
	private Circle flyWeightOtherCircle = new Circle();
	
	@SuppressWarnings("unchecked")
	public SlimeCollisionProcessor() {
		super(Aspect.getAspectForAll(CircleCollision.class, ActiveCollision.class, Flag.class));
	}

	@Override
	protected void initialize() {
		pm = ComponentMapper.getFor(Position.class, world);
		scm = ComponentMapper.getFor(CircleCollision.class, world);
		acm = ComponentMapper.getFor(ActiveCollision.class, world);
	}
	
	protected void process(Entity e) {
		if(acm.getSafe(e) == null) {
			return;
		}


		flyWeightselfCircle.set(pm.get(e).pos, scm.get(e).radius);
		
		GridMapService.getEntities(colFlag, flyWeightselfCircle, entityBagFlyWeight);
		Entity otherEntity;

		for (int i = entityBagFlyWeight.size()-1; i >= 0; i--) {
			otherEntity = world.getEntity(entityBagFlyWeight.get(i));
			flyWeightOtherCircle.set(pm.get(otherEntity).pos, scm.get(otherEntity).radius);
			if (flyWeightselfCircle.contains(flyWeightOtherCircle) && otherEntity.id != e.id) {
				flyWeightselfCircle.setRadius(getRadiusOfCircle(flyWeightselfCircle.area() + flyWeightOtherCircle.area()));
				flyWeightOtherCircle.radius = 0;
				otherEntity.deleteFromWorld();
			}
		}
		entityBagFlyWeight.clear();
	}

	private final float getRadiusOfCircle(double areaOfCircle) {
		return (float)Math.sqrt(areaOfCircle/Math.PI);
	}
}
