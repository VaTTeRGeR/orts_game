package de.vatterger.game.components.gameobject;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

public class Position extends Component {
	public Vector3 v = new Vector3();

	public Position(float x, float y, float z) {
		v.x = x;
		v.y = y;
		v.z = z;
	}
}
