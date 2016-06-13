package de.vatterger.game.components;

import com.artemis.Component;

public class Position extends Component {
	public float x,y,z;

	public Position(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
