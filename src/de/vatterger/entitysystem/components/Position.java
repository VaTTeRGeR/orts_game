package de.vatterger.entitysystem.components;


import com.artemis.Component;
import com.badlogic.gdx.math.Vector2;

public class Position extends Component {
	public Vector2 pos = new Vector2(Vector2.Zero);

	public Position() {
	}
	
	public Position(Vector2 pos) {
		this.pos = pos;
	}
}
