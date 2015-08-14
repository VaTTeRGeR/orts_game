package de.vatterger.entitysystem.components;


import com.artemis.Component;
import com.badlogic.gdx.math.Circle;

public class CircleContainmentOccured extends Component {
	public Circle other = null;
	
	public CircleContainmentOccured() {}

	public CircleContainmentOccured(Circle other) {
		this.other = other;
	}
	
}
