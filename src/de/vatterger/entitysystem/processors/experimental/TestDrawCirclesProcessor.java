package de.vatterger.entitysystem.processors.experimental;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import de.vatterger.entitysystem.components.server.ServerPosition;
import de.vatterger.entitysystem.components.shared.CircleCollision;

@Wire
public class TestDrawCirclesProcessor extends EntityProcessingSystem {

	private ComponentMapper<ServerPosition>	pm;
	private ComponentMapper<CircleCollision>	cm;
	private ShapeRenderer shapes;

	@SuppressWarnings("unchecked")
	public TestDrawCirclesProcessor(ShapeRenderer shapes) {
		super(Aspect.getAspectForAll(ServerPosition.class, CircleCollision.class));
		this.shapes = shapes;
	}

	@Override
	protected void begin() {
		shapes.begin(ShapeType.Line);
		shapes.setColor(Color.WHITE);
	}

	protected void process(Entity e) {
		ServerPosition pc = pm.get(e);
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
