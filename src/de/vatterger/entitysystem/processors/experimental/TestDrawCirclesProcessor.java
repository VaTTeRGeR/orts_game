package de.vatterger.entitysystem.processors.experimental;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import de.vatterger.entitysystem.components.CircleCollision;
import de.vatterger.entitysystem.components.server.ServerPosition;

public class TestDrawCirclesProcessor extends EntityProcessingSystem {

	ComponentMapper<ServerPosition>	pm;
	ComponentMapper<CircleCollision>	cm;
	ShapeRenderer shapes;

	@SuppressWarnings("unchecked")
	public TestDrawCirclesProcessor(ShapeRenderer shapes) {
		super(Aspect.getAspectForAll(ServerPosition.class, CircleCollision.class));
		this.shapes = shapes;
	}

	@Override
	protected void initialize() {
		pm = world.getMapper(ServerPosition.class);
		cm = world.getMapper(CircleCollision.class);
	}

	@Override
	protected void begin() {
		shapes.begin(ShapeType.Line);
		shapes.setColor(Color.WHITE);
	}

	protected void process(Entity e) {
		ServerPosition pc = pm.get(e);
		CircleCollision cc = cm.get(e);
		
		shapes.circle(pc.pos.x, pc.pos.z, cc.radius, 8);
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
