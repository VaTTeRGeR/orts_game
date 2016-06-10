package de.vatterger.techdemo.processors.experimental;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;

import de.vatterger.techdemo.components.client.InterpolatedPosition;
import de.vatterger.techdemo.components.shared.Inactive;

@Wire
public class TestDrawModelLightsProcessor extends EntityProcessingSystem {

	private ComponentMapper<InterpolatedPosition>	cpm;
	
	private Environment environment;
	private Bag<PointLight> modelLights = new Bag<PointLight>();
	
	@SuppressWarnings("unchecked")
	public TestDrawModelLightsProcessor(Environment environment) {
		super(Aspect.all(InterpolatedPosition.class).exclude(Inactive.class));
		this.environment = environment;
	}
	
	@Override
	public void inserted(Entity e) {
		modelLights.set(e.getId(), new PointLight());
		environment.add(modelLights.get(e.getId()));
	}
	
	@Override
	public void removed(Entity e) {
		environment.remove(modelLights.get(e.getId()));
		modelLights.set(e.getId(), null);
	}

	protected void process(Entity e) {
		modelLights.get(e.getId()).position.set(cpm.get(e).getInterpolatedValue());
		modelLights.get(e.getId()).color.set(Color.RED);
		modelLights.get(e.getId()).intensity = 10f;
	}
}
