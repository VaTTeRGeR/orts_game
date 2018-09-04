package de.vatterger.game.systems.graphics;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;

import de.vatterger.game.components.gameobject.SpriteFrame;

public class AnimatedSpriteSystem extends IteratingSystem {

	private ComponentMapper<SpriteFrame> sfm;
	
	private float tDeltaMillis = 0f;

	public AnimatedSpriteSystem() {
		super(Aspect.all(SpriteFrame.class));
	}
	
	@Override
	protected void begin() {
		tDeltaMillis = world.getDelta() * 1000f; //tDelta in milliseconds!
	}
	
	protected void process(int e) {
		
		SpriteFrame sf = sfm.get(e);
		
		if(sf.currentIntervalLeft <= 0f) {
			
			while(sf.currentIntervalLeft <= 0f) {
				sf.currentframe++;
				sf.currentIntervalLeft += sf.interval;
			}
			
			if(sf.currentframe >= sf.numFrames) {
				world.delete(e);
			}
			
		}
		
		sf.currentIntervalLeft -= tDeltaMillis;
		
		sf.currentframe = MathUtils.clamp(sf.currentframe, 0, sf.numFrames - 1);
	}
}
