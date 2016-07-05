package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class Position extends Component {
	public float[] v = new float[3];

	public Position(float x, float y, float z) {
		v[0] = x;
		v[1] = y;
		v[2] = z;
	}
}
