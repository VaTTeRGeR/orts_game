package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class CullDistance extends Component {
	public float dst;
	
	public float offsetX;
	public float offsetY;
	
	public boolean visible = true;

	public CullDistance() {}
	
	public CullDistance(float v) {
		this.dst = v;
	}
	
	public CullDistance(float v, float offsetX, float offsetY) {
		this(v);
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}
}
