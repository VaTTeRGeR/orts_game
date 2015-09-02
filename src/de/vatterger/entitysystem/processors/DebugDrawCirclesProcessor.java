package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import de.vatterger.entitysystem.components.CircleCollision;
import de.vatterger.entitysystem.components.Position;

public class DebugDrawCirclesProcessor extends EntityProcessingSystem {

	ComponentMapper<Position>	pm;
	ComponentMapper<CircleCollision>	cm;
	ShapeRenderer shapes;

	@SuppressWarnings("unchecked")
	public DebugDrawCirclesProcessor(ShapeRenderer shapes) {
		super(Aspect.getAspectForAll(Position.class, CircleCollision.class));
		this.shapes = shapes;
	}

	@Override
	protected void initialize() {
		pm = world.getMapper(Position.class);
		cm = world.getMapper(CircleCollision.class);
	}
	
	@Override
	protected void begin() {
		shapes.begin(ShapeType.Line);
		shapes.setColor(Color.GREEN);
	}

	protected void process(Entity e) {
		Position pc = pm.get(e);
		CircleCollision cc = cm.get(e);
		
		shapes.circle(pc.pos.x, pc.pos.y, cc.radius, 8);
	}
	
	@Override
	protected void end() {
		shapes.end();
	}

	@Override
	protected void dispose() {
		shapes = null;
	}
}
