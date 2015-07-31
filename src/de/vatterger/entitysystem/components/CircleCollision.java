package de.vatterger.entitysystem.components;


import com.artemis.Component;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;

public class CircleCollision extends Component {
	public Circle circle = new Circle(0, 0, 0);
	
	public CircleCollision() {}
	
	public CircleCollision(float radius) {
		circle.setRadius(radius);
	}
}
