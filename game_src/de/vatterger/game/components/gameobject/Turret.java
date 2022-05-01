package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class Turret extends Component {
	
	public float angleMin, angleMax;
	
	public Turret () {
		angleMin = 0f;
		angleMax = 360f;
	}
	
	public Turret (float angleMin, float angleMax) {
		this.angleMin = angleMin;
		this.angleMax = angleMax;
	}
}
