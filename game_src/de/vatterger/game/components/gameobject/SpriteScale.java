package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class SpriteScale extends Component {
	
	public float scale = 1f;
	
	public SpriteScale() {}
	
	public SpriteScale(float scale) {
		this.scale = scale;
	}
}
