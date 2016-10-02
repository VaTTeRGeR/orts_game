package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class AbsoluteRotation extends Component {
	public float rotation;

	public AbsoluteRotation() {
		this.rotation = 0;
	}
	
	public AbsoluteRotation(float rotation){
		this.rotation = rotation;
	}
}
