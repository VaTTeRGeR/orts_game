package de.vatterger.game.components.gameobject;

import com.artemis.Component;
import com.artemis.annotations.EntityId;

public class CullingParent extends Component {
	
	@EntityId public int parent = -1;
	
	public CullingParent() {}
	
	public CullingParent(int parent) {
		this.parent = parent;
	}
}
