package de.vatterger.game.components;

import com.artemis.Component;

public class LocalPosition extends Component {
	public float[] v = new float[3];

	public LocalPosition(float x, float y, float z) {
		v[0] = x;
		v[1] = y;
		v[2] = z;
	}
}
