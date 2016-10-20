package de.vatterger.game.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

import de.vatterger.game.components.gameobject.CullDistance;
import de.vatterger.game.components.gameobject.RemoveTimed;

public class RemoveTimedSystem extends IteratingSystem {

	private ComponentMapper<RemoveTimed> rtm;
	private ComponentMapper<CullDistance> cdm;
	
	public RemoveTimedSystem() {
		super(Aspect.all(RemoveTimed.class, CullDistance.class));
	}

	@Override
	protected void process(int e) {
		if(!cdm.get(e).visible) {
			rtm.get(e).timeLeft-=world.getDelta();
			if(rtm.get(e).timeLeft <= 0) {
				world.delete(e);
			}
		}
	}
}
