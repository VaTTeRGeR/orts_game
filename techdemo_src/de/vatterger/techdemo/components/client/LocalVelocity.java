package de.vatterger.techdemo.components.client;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

public class LocalVelocity extends Component {
	public Vector3 vel = new Vector3(Vector3.Zero);

	public LocalVelocity(){}

	public LocalVelocity(Vector3 vel) {
		this.vel = vel;
	}
}
