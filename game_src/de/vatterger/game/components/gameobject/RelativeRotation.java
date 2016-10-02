package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class RelativeRotation extends Component {
	public float rotation;

	public RelativeRotation() {
		this.rotation = 0;
	}
	
	public RelativeRotation(float rotation){
		this.rotation = rotation;
	}
}
