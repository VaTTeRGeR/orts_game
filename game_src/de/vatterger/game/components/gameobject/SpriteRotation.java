package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class SpriteRotation extends Component {
	public float[] rotation = null;

	public SpriteRotation() {}
	
	public SpriteRotation(float[] rotation){
		this.rotation = rotation;
	}
}
