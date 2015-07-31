package de.vatterger.entitysystem.components;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

public class Velocity extends Component{
	public Vector3 vel = new Vector3(Vector3.Zero);
	
	public Velocity(){}

	public Velocity(Vector3 vel) {
		this.vel = vel;
	}
}
