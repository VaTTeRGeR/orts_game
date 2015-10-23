package de.vatterger.entitysystem.processors_test;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import de.vatterger.entitysystem.components.ClientPosition;
import de.vatterger.entitysystem.components.shared.Inactive;

@Wire
public class TestDrawModelLightsProcessor extends EntityProcessingSystem {

	private ComponentMapper<ClientPosition>	cpm;
	
	private Environment environment;
	private Bag<PointLight> modelLights = new Bag<PointLight>();
	
	@SuppressWarnings("unchecked")
	public TestDrawModelLightsProcessor(Environment environment) {
		super(Aspect.getAspectForAll(ClientPosition.class).exclude(Inactive.class));
		this.environment = environment;
	}
	
	@Override
	protected void inserted(Entity e) {
		modelLights.set(e.id, new PointLight());
		environment.add(modelLights.get(e.id));
	}
	
	@Override
	protected void removed(Entity e) {
		environment.remove(modelLights.get(e.id));
		modelLights.set(e.id, null);
	}

	protected void process(Entity e) {
		modelLights.get(e.id).position.set(cpm.get(e).getInterpolatedValue()).add(0f, 0f, 3f);
		modelLights.get(e.id).color.set(Color.ORANGE);
		modelLights.get(e.id).intensity = 10f;
	}
}
