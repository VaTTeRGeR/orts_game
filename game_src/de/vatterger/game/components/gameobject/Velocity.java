package de.vatterger.game.components.gameobject;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

public class Velocity extends Component {
	public Vector3 velocity = new Vector3();

	public Velocity() {}
	
	public Velocity(float vx, float vy, float vz) {
		velocity.x = vx;
		velocity.y = vy;
		velocity.z = vz;
	}
}
