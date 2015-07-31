package de.vatterger.entitysystem.components;


import com.artemis.Component;
import com.badlogic.gdx.math.Circle;

public class CircleCollisionOccured extends Component {
	public Circle other = null;
	
	public CircleCollisionOccured() {}

	public CircleCollisionOccured(Circle other) {
		this.other = other;
	}
	
}
