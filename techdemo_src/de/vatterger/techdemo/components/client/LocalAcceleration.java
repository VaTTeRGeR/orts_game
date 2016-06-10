package de.vatterger.techdemo.components.client;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

public class LocalAcceleration extends Component {
	public Vector3 acc = new Vector3(Vector3.Zero);

	public LocalAcceleration(){}

	public LocalAcceleration(Vector3 acc) {
		this.acc = acc;
	}
}
