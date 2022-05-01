package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class CollisionRadius extends Component {
	
	public float dst;
	
	public float offsetX;
	public float offsetY;
	
	public CollisionRadius() {}
	
	public CollisionRadius(float v) {
		this.dst = v;
	}
	
	public CollisionRadius(float v, float offsetX, float offsetY) {
		this(v);
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}
}
