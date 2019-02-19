package de.vatterger.game.systems.gameplay;

import java.util.HashMap;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

import de.vatterger.game.components.gameobject.RemoveTimed;
import de.vatterger.game.components.gameobject.SpriteDrawMode;

public class FadeSpriteSystem extends IteratingSystem {

	private ComponentMapper<RemoveTimed> rtm;
	private ComponentMapper<SpriteDrawMode> sdmm;
	
	private HashMap<Integer, Float> baseTimes = new HashMap<>(512);
	
	public FadeSpriteSystem() {
		super(Aspect.all(RemoveTimed.class, SpriteDrawMode.class));
	}
	
	@Override
	protected void inserted(int entityId) {
		float baseTime = rtm.get(entityId).timeLeft;
		baseTimes.put(entityId, baseTime);
	}

	@Override
	protected void removed(int entityId) {
		baseTimes.remove(entityId);
	}
	
	@Override
	protected void process(int e) {
		sdmm.get(e).color.a = rtm.get(e).timeLeft/baseTimes.getOrDefault(e, 100f);
	}
}
