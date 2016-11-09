package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class CullDistance extends Component {
	public float dst;
	public boolean visible = true;

	public CullDistance() {}
	
	public CullDistance(float v) {
		this.dst = v;
	}
}
