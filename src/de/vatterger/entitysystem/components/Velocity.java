package de.vatterger.entitysystem.components;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector2;

public class Velocity extends Component{
	public Vector2 vel = new Vector2(Vector2.Zero);
	
	public Velocity(){}

	public Velocity(Vector2 vel) {
		this.vel = vel;
	}
}
