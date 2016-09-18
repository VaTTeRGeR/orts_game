package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class SpriteRotation extends Component {
	public float rotation;

	public SpriteRotation() {
		this.rotation = 0;
	}
	
	public SpriteRotation(float rotation){
		this.rotation = rotation;
	}
}
