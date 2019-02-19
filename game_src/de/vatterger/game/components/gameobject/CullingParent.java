package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class CullingParent extends Component {
	
	public int parent = -1;
	
	public CullingParent() {}
	
	public CullingParent(int parent) {
		this.parent = parent;
	}
}
